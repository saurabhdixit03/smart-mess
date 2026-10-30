package com.smartmess.backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.request.SubmitMealFeedbackRequest;
import com.smartmess.backend.dto.request.SubmitMealFeedbackRequest.DishRating;
import com.smartmess.backend.dto.response.PendingMealFeedbackResponse;
import com.smartmess.backend.dto.response.PendingMealFeedbackResponse.DishFeedbackItem;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.entity.DishFeedbackRating;
import com.smartmess.backend.entity.MealFeedback;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealFeedbackRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.repository.DishFeedbackRatingRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.MealFeedbackService;

@Service
public class MealFeedbackServiceImpl implements MealFeedbackService {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final int MAX_COMMENT_LENGTH = 1000;

    private final MealFeedbackRepository mealFeedbackRepository;
    private final DishFeedbackRatingRepository dishFeedbackRatingRepository;
    private final MealRecordRepository mealRecordRepository;
    private final CustomerRepository customerRepository;
    private final CustomerSecurity customerSecurity;

    public MealFeedbackServiceImpl(
            MealFeedbackRepository mealFeedbackRepository,
            DishFeedbackRatingRepository dishFeedbackRatingRepository,
            MealRecordRepository mealRecordRepository,
            CustomerRepository customerRepository,
            CustomerSecurity customerSecurity) {

        this.mealFeedbackRepository = mealFeedbackRepository;
        this.dishFeedbackRatingRepository = dishFeedbackRatingRepository;
        this.mealRecordRepository = mealRecordRepository;
        this.customerRepository = customerRepository;
        this.customerSecurity = customerSecurity;
    }

    @Transactional(readOnly = true)
    @Override
    public PendingMealFeedbackResponse getPendingFeedback() {

        Long messId = customerSecurity.getCurrentMessId();
        Long customerId = customerSecurity.getCurrentUserId();

        Customer customer = customerRepository
                .findByCustomerIdAndMess_MessId(
                        customerId,
                        messId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found."
                ));

        List<MealRecord> mealRecords =
                mealRecordRepository
                        .findByMess_MessIdAndCustomerOrderByCollectedAtDesc(
                                messId,
                                customer
                        );

        /*
         * No completed meal means there is nothing to review.
         */
        if (mealRecords.isEmpty()) {
            return null;
        }

        /*
         * Only the most recent completed meal is eligible.
         *
         * We intentionally do not walk backwards through older
         * unreviewed meals. This follows the agreed feedback flow.
         */
        MealRecord latestMealRecord = mealRecords.get(0);

        /*
         * Once feedback exists for the latest completed meal,
         * there is nothing pending.
         */
        if (mealFeedbackRepository.existsByMess_MessIdAndMealRecord(
                messId,
                latestMealRecord
        )) {
            return null;
        }

        Menu menu = latestMealRecord.getMenu();

        List<DishFeedbackItem> dishes =
                extractDishes(menu);

        if (dishes.isEmpty()) {
            return null;
        }

        return new PendingMealFeedbackResponse(
                latestMealRecord.getMealRecordId(),
                menu.getMenuDate(),
                menu.getMealSession(),
                dishes
        );
    }

    @Transactional
    @Override
    public void submitFeedback(
            SubmitMealFeedbackRequest request) {

        validateRequest(request);

        Long messId = customerSecurity.getCurrentMessId();
        Long customerId = customerSecurity.getCurrentUserId();

        Customer customer = customerRepository
                .findByCustomerIdAndMess_MessId(
                        customerId,
                        messId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found."
                ));

        MealRecord mealRecord = mealRecordRepository
                .findByMealRecordIdAndMess_MessId(
                        request.mealRecordId(),
                        messId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Meal record not found with ID: "
                                + request.mealRecordId()
                ));

        
        if (!mealRecord.getCustomer().getCustomerId()
                .equals(customerId)) {
            throw new AccessDeniedException(
                    "Meal record does not belong to the authenticated customer."
            );
        }

        /*
         * One feedback submission is allowed per completed meal.
         */
        if (mealFeedbackRepository.existsByMess_MessIdAndMealRecord(
                messId,
                mealRecord
        )) {
            throw new BusinessException(
                    "Feedback has already been submitted for this meal."
            );
        }

        List<Dish> mealDishes =
                extractDishEntities(mealRecord.getMenu());

        if (mealDishes.isEmpty()) {
            throw new BusinessException(
                    "No dishes are available for this meal."
            );
        }

        validateDishRatings(
                request.dishRatings(),
                mealDishes
        );

        String commonComment = normalizeComment(
                request.commonComment()
        );

        MealFeedback feedback = new MealFeedback();
        feedback.setMess(mealRecord.getMess());
        feedback.setCustomer(customer);
        feedback.setMealRecord(mealRecord);
        feedback.setCommonComment(commonComment);

        MealFeedback savedFeedback =
                mealFeedbackRepository.saveAndFlush(feedback);

        for (DishRating dishRating : request.dishRatings()) {

            Dish dish = findDishForMeal(
                    mealDishes,
                    dishRating.dishId()
            );

            DishFeedbackRating rating = new DishFeedbackRating();
            rating.setMealFeedback(savedFeedback);
            rating.setDish(dish);
            rating.setRating(dishRating.rating());

            dishFeedbackRatingRepository.save(rating);
        }
    }

    private List<DishFeedbackItem> extractDishes(Menu menu) {

        List<DishFeedbackItem> dishes = new ArrayList<>();
        Set<Long> addedDishIds = new HashSet<>();

        addDishResponse(
                menu.getSabjiOne(),
                dishes,
                addedDishIds
        );

        addDishResponse(
                menu.getSabjiTwo(),
                dishes,
                addedDishIds
        );

        addDishResponse(
                menu.getDal(),
                dishes,
                addedDishIds
        );

        addDishResponse(
                menu.getRice(),
                dishes,
                addedDishIds
        );

        addDishResponse(
                menu.getSweet(),
                dishes,
                addedDishIds
        );

        return dishes;
    }

    private void addDishResponse(
            Dish dish,
            List<DishFeedbackItem> dishes,
            Set<Long> addedDishIds) {

        if (dish == null) {
            return;
        }

        if (addedDishIds.add(dish.getDishId())) {
            dishes.add(
                    new DishFeedbackItem(
                            dish.getDishId(),
                            dish.getName()
                    )
            );
        }
    }

    private List<Dish> extractDishEntities(Menu menu) {

        List<Dish> dishes = new ArrayList<>();
        Set<Long> addedDishIds = new HashSet<>();

        addDishEntity(menu.getSabjiOne(), dishes, addedDishIds);
        addDishEntity(menu.getSabjiTwo(), dishes, addedDishIds);
        addDishEntity(menu.getDal(), dishes, addedDishIds);
        addDishEntity(menu.getRice(), dishes, addedDishIds);
        addDishEntity(menu.getSweet(), dishes, addedDishIds);

        return dishes;
    }

    private void addDishEntity(
            Dish dish,
            List<Dish> dishes,
            Set<Long> addedDishIds) {

        if (dish == null) {
            return;
        }

        if (addedDishIds.add(dish.getDishId())) {
            dishes.add(dish);
        }
    }

    private void validateDishRatings(
            List<DishRating> ratings,
            List<Dish> mealDishes) {

        if (ratings == null || ratings.isEmpty()) {
            throw new BusinessException(
                    "A rating is required for every dish."
            );
        }

        Set<Long> expectedDishIds = mealDishes.stream()
                .map(Dish::getDishId)
                .collect(java.util.stream.Collectors.toSet());

        Set<Long> submittedDishIds = new HashSet<>();

        for (DishRating dishRating : ratings) {

            if (dishRating == null
                    || dishRating.dishId() == null
                    || dishRating.rating() == null) {
                throw new BusinessException(
                        "Each dish rating must contain a dish ID and rating."
                );
            }

            if (!expectedDishIds.contains(dishRating.dishId())) {
                throw new BusinessException(
                        "Dish ID "
                                + dishRating.dishId()
                                + " does not belong to this meal."
                );
            }

            if (!submittedDishIds.add(dishRating.dishId())) {
                throw new BusinessException(
                        "Duplicate rating found for dish ID: "
                                + dishRating.dishId()
                );
            }

            if (dishRating.rating() < MIN_RATING
                    || dishRating.rating() > MAX_RATING) {
                throw new BusinessException(
                        "Dish rating must be between "
                                + MIN_RATING
                                + " and "
                                + MAX_RATING
                                + "."
                );
            }
        }

        /*
         * Every dish served in the completed meal must receive
         * exactly one rating.
         */
        if (!submittedDishIds.equals(expectedDishIds)) {
            throw new BusinessException(
                    "A rating is required for every dish in the meal."
            );
        }
    }

    private Dish findDishForMeal(
            List<Dish> mealDishes,
            Long dishId) {

        return mealDishes.stream()
                .filter(dish -> dish.getDishId().equals(dishId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Dish does not belong to this meal."
                ));
    }

    private void validateRequest(
            SubmitMealFeedbackRequest request) {

        if (request == null
                || request.mealRecordId() == null) {
            throw new BusinessException(
                    "Meal record ID is required."
            );
        }

        if (request.mealRecordId() <= 0) {
            throw new BusinessException(
                    "Meal record ID must be positive."
            );
        }
    }

    private String normalizeComment(String comment) {

        if (comment == null) {
            return null;
        }

        String normalized = comment.strip();

        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > MAX_COMMENT_LENGTH) {
            throw new BusinessException(
                    "Feedback comment must not exceed "
                            + MAX_COMMENT_LENGTH
                            + " characters."
            );
        }

        return normalized;
    }
}