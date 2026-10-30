package com.smartmess.backend.config.seed;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.entity.DishFeedbackRating;
import com.smartmess.backend.entity.MealFeedback;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.repository.DishFeedbackRatingRepository;
import com.smartmess.backend.repository.MealFeedbackRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.repository.MenuRepository;

@Component
public class MealFeedbackSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(MealFeedbackSeeder.class);

    private static final long RANDOM_SEED =
            20260917L;

    /*
     * Approximately 65% of historical meal records
     * receive feedback.
     */
    private static final double FEEDBACK_RATE =
            0.65;

    /*
     * These dishes have intentional long-term patterns
     * in their ratings so future analytics can discover
     * useful trends.
     */
    private static final String BHINDI_MASALA =
            "bhindi masala";

    private static final String PANEER_BHURJI =
            "paneer bhurji";

    private static final String ALOO_MATAR =
            "aloo matar";

    private static final String BAINGAN_MASALA =
            "baingan masala";

    private static final String MIX_VEG =
            "mix veg";

    private record GeneratedComment(
            String text,
            Dish commentedDish) {
    }

    private final MealFeedbackRepository mealFeedbackRepository;
    private final DishFeedbackRatingRepository dishFeedbackRatingRepository;
    private final MealRecordRepository mealRecordRepository;
    private final MenuRepository menuRepository;
    private final Clock clock;

    public MealFeedbackSeeder(
            MealFeedbackRepository mealFeedbackRepository,
            DishFeedbackRatingRepository dishFeedbackRatingRepository,
            MealRecordRepository mealRecordRepository,
            MenuRepository menuRepository,
            Clock clock) {

        this.mealFeedbackRepository =
                mealFeedbackRepository;

        this.dishFeedbackRatingRepository =
                dishFeedbackRatingRepository;

        this.mealRecordRepository =
                mealRecordRepository;

        this.menuRepository =
                menuRepository;

        this.clock =
                clock;
    }

    /*
     * Seeds realistic historical feedback for the
     * supplied demo mess.
     *
     * Meal Records must already exist because feedback
     * belongs to an actual completed meal occurrence.
     */
    @Transactional
    public void seed(Mess mess) {

        if (mess == null || mess.getMessId() == null) {
            throw new IllegalArgumentException(
                    "A persisted demo mess is required."
            );
        }

        Long messId =
                mess.getMessId();

        List<Menu> menus =
                menuRepository
                        .findAllByMess_MessIdOrderByMenuDateAscMealSessionAsc(
                                messId
                        );

        if (menus.isEmpty()) {

            log.warn(
                    "Skipping Meal Feedback seeding because no menus "
                            + "are available for demo mess {}.",
                    messId
            );

            return;
        }

        LocalDate today =
                LocalDate.now(clock);

        List<MealRecord> historicalRecords =
                new ArrayList<>();

        for (Menu menu : menus) {

            /*
             * Today's records are intentionally excluded.
             * They remain available for manual customer testing.
             */
            if (!menu.getMenuDate().isBefore(today)) {
                continue;
            }

            historicalRecords.addAll(
                    mealRecordRepository
                            .findByMess_MessIdAndMenu(
                                    messId,
                                    menu
                            )
            );
        }

        historicalRecords.sort(
                Comparator.comparing(
                        MealRecord::getCollectedAt
                )
        );

        if (historicalRecords.isEmpty()) {

            log.warn(
                    "Skipping Meal Feedback seeding because no historical "
                            + "Meal Records are available for demo mess {}.",
                    messId
            );

            return;
        }

        LocalDate firstDate =
                historicalRecords
                        .get(0)
                        .getCollectedAt()
                        .toLocalDate();

        LocalDate lastDate =
                historicalRecords
                        .get(historicalRecords.size() - 1)
                        .getCollectedAt()
                        .toLocalDate();

        long totalDays =
                Math.max(
                        1,
                        java.time.temporal.ChronoUnit.DAYS.between(
                                firstDate,
                                lastDate
                        )
                );

        Random selectionRandom =
                new Random(RANDOM_SEED);

        /*
         * Tracks how many dish-specific comments each dish
         * has already received during this seed run.
         *
         * This keeps comment volume reasonably balanced
         * across dishes without forcing exact equality.
         */
        Map<Long, Integer> dishCommentCounts =
                new HashMap<>();

        int feedbackCreated = 0;
        int recordsSkipped = 0;

        for (MealRecord mealRecord : historicalRecords) {

            /*
             * Protect against accidental duplicate feedback
             * if the seeder is executed again.
             */
            if (mealFeedbackRepository
                    .findByMess_MessIdAndMealRecord(
                            messId,
                            mealRecord
                    )
                    .isPresent()) {

                recordsSkipped++;
                continue;
            }

            /*
             * Leave a realistic portion of historical meals
             * without feedback.
             */
            if (selectionRandom.nextDouble()
                    >= FEEDBACK_RATE) {

                continue;
            }

            List<Dish> dishes =
                    extractDishes(
                            mealRecord.getMenu()
                    );

            if (dishes.isEmpty()) {
                continue;
            }

            double progress =
                    calculateProgress(
                            mealRecord
                                    .getCollectedAt()
                                    .toLocalDate(),
                            firstDate,
                            totalDays
                    );

            MealFeedback feedback =
                    createMealFeedback(
                            mess,
                            mealRecord,
                            dishes,
                            progress,
                            dishCommentCounts
                    );

            feedback =
                    mealFeedbackRepository.save(
                            feedback
                    );

            List<DishFeedbackRating> ratings =
                    createDishRatings(
                            feedback,
                            dishes,
                            progress
                    );

            dishFeedbackRatingRepository.saveAll(
                    ratings
            );

            feedbackCreated++;
        }

        log.info(
                "Demo Meal Feedback seeded successfully for mess {}. "
                        + "Historical Meal Records: {}, Feedback Created: {}, "
                        + "Already Existing: {}.",
                messId,
                historicalRecords.size(),
                feedbackCreated,
                recordsSkipped
        );
    }

    private MealFeedback createMealFeedback(
            Mess mess,
            MealRecord mealRecord,
            List<Dish> dishes,
            double progress,
            Map<Long, Integer> dishCommentCounts) {

        MealFeedback feedback =
                new MealFeedback();

        feedback.setMess(mess);

        feedback.setCustomer(
                mealRecord.getCustomer()
        );

        feedback.setMealRecord(
                mealRecord
        );

        /*
         * Comments are intentionally not present on
         * every feedback.
         */
        GeneratedComment generatedComment =
                generateComment(
                        dishes,
                        progress,
                        mealRecord,
                        dishCommentCounts
                );

        feedback.setCommonComment(
                generatedComment.text()
        );

        feedback.setCommentedDish(
                generatedComment.commentedDish()
        );

        return feedback;
    }

    private List<DishFeedbackRating> createDishRatings(
            MealFeedback feedback,
            List<Dish> dishes,
            double progress) {

        List<DishFeedbackRating> ratings =
                new ArrayList<>();

        for (Dish dish : dishes) {

            int rating =
                    generateRating(
                            dish,
                            feedback.getMealRecord(),
                            progress
                    );

            DishFeedbackRating ratingEntity =
                    new DishFeedbackRating();

            ratingEntity.setMealFeedback(
                    feedback
            );

            ratingEntity.setDish(
                    dish
            );

            ratingEntity.setRating(
                    rating
            );

            ratings.add(
                    ratingEntity
            );
        }

        return ratings;
    }

    /*
     * Generates ratings with two goals:
     *
     * 1. The same dish can receive different ratings
     *    from different customers.
     *
     * 2. Selected dishes still have meaningful trends
     *    for analytics.
     */
    private int generateRating(
            Dish dish,
            MealRecord mealRecord,
            double progress) {

        String normalizedName =
                dish.getNormalizedName();

        double baseRating;

        if (BHINDI_MASALA.equals(normalizedName)) {
            /*
             * Starts weaker and gradually improves.
             */
            baseRating =
                    2.6 + (1.8 * progress);

        } else if (PANEER_BHURJI.equals(normalizedName)) {
            /*
             * Strong performer, but not universally loved.
             */
            baseRating =
                    4.2;

        } else if (ALOO_MATAR.equals(normalizedName)) {
            /*
             * Starts reasonably well and gradually declines.
             */
            baseRating =
                    4.3 - (1.7 * progress);

        } else if (BAINGAN_MASALA.equals(normalizedName)) {
            /*
             * Generally weaker, but individual opinions vary.
             */
            baseRating =
                    2.8;

        } else if (MIX_VEG.equals(normalizedName)) {
            /*
             * Deliberately mixed customer experience.
             */
            baseRating =
                    3.3;

        } else {
            /*
             * Normal dishes are generally positive,
             * but individual customers can disagree.
             */
            baseRating =
                    3.7;
        }

        /*
         * Customer-specific deterministic variation.
         *
         * The seed uses the meal record and dish, so different
         * meal occurrences can produce different ratings for
         * the same dish.
         */
        long seed =
                RANDOM_SEED
                        + (mealRecord.getMealRecordId() * 31L)
                        + (dish.getDishId() * 17L);

        Random random =
                new Random(seed);

        /*
         * Wider variation creates more realistic disagreement
         * between customers.
         *
         * This allows strong dishes to occasionally receive
         * lower ratings and weaker dishes to occasionally
         * receive higher ratings.
         */
        double variation =
                (random.nextDouble() * 2.8) - 1.4;

        double ratingValue =
                baseRating + variation;

        /*
         * Small number of stronger individual opinions.
         *
         * This introduces occasional 1★/2★ or 5★ ratings
         * without making extreme ratings common.
         */
        double extremeChance =
                random.nextDouble();

        if (extremeChance < 0.04) {
            ratingValue -= 1.0;
        } else if (extremeChance > 0.96) {
            ratingValue += 0.8;
        }

        int rating =
                (int) Math.round(ratingValue);

        /*
         * Keep rating within the valid 1–5 range.
         */
        return Math.max(
                1,
                Math.min(
                        5,
                        rating
                )
        );
    }

    /*
     * Generates comments independently from ratings.
     *
     * This is intentional.
     *
     * Example:
     *
     * 5 stars + "a little oily"
     * 3 stars + "good overall"
     * 2 stars + "too oily"
     *
     * This better represents different customer preferences.
     */
    private GeneratedComment generateComment(
            List<Dish> dishes,
            double progress,
            MealRecord mealRecord,
            Map<Long, Integer> dishCommentCounts) {

        /*
         * Approximately 60% of feedback contains comments.
         */
        long commentPattern =
                Math.floorMod(
                        mealRecord.getMealRecordId(),
                        10
                );

        if (commentPattern >= 6) {

            return new GeneratedComment(
                    null,
                    null
            );
        }

        Random commentRandom =
                new Random(
                        RANDOM_SEED
                                + mealRecord.getMealRecordId() * 17L
                );

        /*
         * Approximately 25% of comments are about
         * the overall meal.
         */
        if (commentRandom.nextDouble() < 0.25) {

            String comment;

            int wordingPattern =
                    commentRandom.nextInt(5);

            switch (wordingPattern) {

                case 0 ->
                        comment =
                                "Overall the meal was good.";

                case 1 ->
                        comment =
                                "The meal was quite enjoyable today.";

                case 2 ->
                        comment =
                                "Nice meal today.";

                case 3 ->
                        comment =
                                "The overall meal was decent.";

                default ->
                        comment =
                                "I enjoyed most of the meal today.";
            }

            return new GeneratedComment(
                    comment,
                    null
            );
        }

        /*
         * From this point the comment is dish-specific.
         *
         * Choose among the least-commented dishes currently
         * present in this meal. This keeps dish comment volume
         * reasonably balanced without forcing exact equality.
         */
        int minimumCommentCount =
                dishes.stream()
                        .mapToInt(
                                dish ->
                                        dishCommentCounts.getOrDefault(
                                                dish.getDishId(),
                                                0
                                        )
                        )
                        .min()
                        .orElse(0);

        List<Dish> leastCommentedDishes =
                dishes.stream()
                        .filter(
                                dish ->
                                        dishCommentCounts.getOrDefault(
                                                dish.getDishId(),
                                                0
                                        ) == minimumCommentCount
                        )
                        .toList();

        Dish commentedDish =
                leastCommentedDishes.get(
                        commentRandom.nextInt(
                                leastCommentedDishes.size()
                        )
                );

        dishCommentCounts.merge(
                commentedDish.getDishId(),
                1,
                Integer::sum
        );

        String normalizedName =
                commentedDish.getNormalizedName();

        /*
         * Selected dishes have several possible opinions.
         * No dish is locked to a single sentiment.
         */

        if (BHINDI_MASALA.equals(normalizedName)) {

            String[] comments = {
                    "Bhindi was nicely prepared today.",
                    "The bhindi tasted good overall.",
                    "Bhindi was a little too oily for me.",
                    "The taste was good, but the bhindi felt oily.",
                    "Could reduce the oil slightly in the bhindi.",
                    "I really liked the bhindi today."
            };

            return new GeneratedComment(
                    comments[
                            commentRandom.nextInt(
                                    comments.length
                            )
                    ],
                    commentedDish
            );
        }

        if (PANEER_BHURJI.equals(normalizedName)) {

            String[] comments = {
                    "Paneer bhurji was really tasty.",
                    "I really liked the paneer bhurji today.",
                    "The paneer was soft and flavorful.",
                    "The paneer bhurji was good overall.",
                    "The masala could use a little more flavour.",
                    "The paneer bhurji was okay, but not my favourite today."
            };

            return new GeneratedComment(
                    comments[
                            commentRandom.nextInt(
                                    comments.length
                            )
                    ],
                    commentedDish
            );
        }

        if (ALOO_MATAR.equals(normalizedName)) {

            String[] comments = {
                    "Aloo matar was tasty today.",
                    "The aloo matar was quite good.",
                    "I liked the aloo matar today.",
                    "The taste of aloo matar felt a little off today.",
                    "The aloo matar could use more flavour.",
                    "The aloo matar was okay overall."
            };

            return new GeneratedComment(
                    comments[
                            commentRandom.nextInt(
                                    comments.length
                            )
                    ],
                    commentedDish
            );
        }

        if (BAINGAN_MASALA.equals(normalizedName)) {

            String[] comments = {
                    "Baingan masala could use more flavour.",
                    "The baingan was not very enjoyable today.",
                    "I liked the baingan masala today.",
                    "The baingan was decent overall.",
                    "The baingan tasted better than expected today.",
                    "The baingan could use a little more seasoning."
            };

            return new GeneratedComment(
                    comments[
                            commentRandom.nextInt(
                                    comments.length
                            )
                    ],
                    commentedDish
            );
        }

        if (MIX_VEG.equals(normalizedName)) {

            String[] comments = {
                    "The mix veg was decent today.",
                    "I liked the mix veg today.",
                    "Mix veg was okay, but could be better.",
                    "The mix veg had good flavour today.",
                    "The mix veg could use a little more seasoning.",
                    "I was not very impressed with the mix veg today."
            };

            return new GeneratedComment(
                    comments[
                            commentRandom.nextInt(
                                    comments.length
                            )
                    ],
                    commentedDish
            );
        }

        /*
         * Generic comments for all other dishes.
         *
         * These also contain positive, neutral and negative
         * opinions so that no dish becomes universally liked.
         */
        String dishName =
                commentedDish.getName();

        String[] comments = {
                dishName + " was really tasty today.",
                "I liked the " + dishName + " today.",
                "The " + dishName + " was good overall.",
                "The " + dishName + " was decent today.",
                "The " + dishName + " could use a little more flavour.",
                "The " + dishName + " could use a little more seasoning.",
                "The " + dishName + " was okay, but could be better.",
                "I really enjoyed the " + dishName + " today."
        };

        return new GeneratedComment(
                comments[
                        commentRandom.nextInt(
                                comments.length
                        )
                ],
                commentedDish
        );
    }

    private List<Dish> extractDishes(
            Menu menu) {

        List<Dish> dishes =
                new ArrayList<>();

        addIfPresent(
                dishes,
                menu.getSabjiOne()
        );

        addIfPresent(
                dishes,
                menu.getSabjiTwo()
        );

        addIfPresent(
                dishes,
                menu.getDal()
        );

        addIfPresent(
                dishes,
                menu.getRice()
        );

        addIfPresent(
                dishes,
                menu.getSweet()
        );

        return dishes;
    }

    private void addIfPresent(
            List<Dish> dishes,
            Dish dish) {

        if (dish == null) {
            return;
        }

        /*
         * Protect against duplicate dish ratings if
         * a future menu configuration references the
         * same dish twice.
         */
        boolean alreadyPresent =
                dishes.stream()
                        .anyMatch(
                                existing ->
                                        existing.getDishId()
                                                .equals(
                                                        dish.getDishId()
                                                )
                        );

        if (!alreadyPresent) {
            dishes.add(dish);
        }
    }

    private double calculateProgress(
            LocalDate date,
            LocalDate firstDate,
            long totalDays) {

        long daysSinceStart =
                java.time.temporal.ChronoUnit.DAYS.between(
                        firstDate,
                        date
                );

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        (double) daysSinceStart
                                / totalDays
                )
        );
    }
}