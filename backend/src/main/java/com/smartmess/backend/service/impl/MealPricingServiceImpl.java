package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.MealPricingResponse;
import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.MealPricingMapper;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.MealPricingService;
import com.smartmess.backend.service.NotificationService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@Service
public class MealPricingServiceImpl implements MealPricingService {

    private static final BigDecimal MAX_PRICE =
            new BigDecimal("99999999.99");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "d MMMM uuuu",
                    Locale.ENGLISH
            );

    private final MealPricingRepository mealPricingRepository;
    private final MealPricingMapper mealPricingMapper;
    private final NotificationService notificationService;
    private final CustomerSecurity customerSecurity;
    private final EntityManager entityManager;
    private final Clock clock;

    public MealPricingServiceImpl(
            MealPricingRepository mealPricingRepository,
            MealPricingMapper mealPricingMapper,
            NotificationService notificationService,
            CustomerSecurity customerSecurity,
            EntityManager entityManager,
            Clock clock) {

        this.mealPricingRepository = mealPricingRepository;
        this.mealPricingMapper = mealPricingMapper;
        this.notificationService = notificationService;
        this.customerSecurity = customerSecurity;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    @Override
    public MealPricingResponse getCurrentPricing() {

        Long messId = customerSecurity.getCurrentMessId();

        MealPricing pricing = mealPricingRepository
                .findTopByMess_MessIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        messId,
                        LocalDateTime.now(clock)
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Meal pricing not configured."
                ));

        return mealPricingMapper.toResponse(pricing);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MealPricingResponse> getScheduledPricing() {

        requireOwner();

        Long messId = customerSecurity.getCurrentMessId();

        /*
         * Return all existing future entries so that any schedules
         * created before the single-entry rule remain visible and
         * can be cancelled explicitly.
         */
        return mealPricingRepository
                .findByMess_MessIdAndEffectiveFromGreaterThanOrderByEffectiveFromAsc(
                        messId,
                        LocalDateTime.now(clock)
                )
                .stream()
                .map(mealPricingMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public MealPricingResponse updatePricing(
            UpdateMealPricingRequest request) {

        requireOwner();

        if (request == null) {
            throw new BusinessException(
                    "Meal pricing request is required."
            );
        }

        validatePrice(request.halfMealPrice(), "Half Meal");
        validatePrice(request.fullMealPrice(), "Full Meal");
        validatePrice(request.extraRotiPrice(), "Extra Roti");

        Long messId = customerSecurity.getCurrentMessId();

        /*
         * Every pricing update and cancellation acquires the same
         * mess lock before inspecting future entries.
         */
        Mess mess = lockMess(messId);

        LocalDateTime now = LocalDateTime.now(clock)
                .truncatedTo(ChronoUnit.MICROS);

        LocalDate today = now.toLocalDate();

        LocalDate effectiveDate = request.effectiveDate() == null
                ? today
                : request.effectiveDate();

        if (effectiveDate.isBefore(today)) {
            throw new BusinessException(
                    "Pricing cannot be scheduled for a past date."
            );
        }

        boolean scheduled = effectiveDate.isAfter(today);

        LocalDateTime effectiveFrom = scheduled
                ? effectiveDate.atStartOfDay()
                : now;

        List<MealPricing> upcomingPricing = mealPricingRepository
                .findByMess_MessIdAndEffectiveFromGreaterThanOrderByEffectiveFromAsc(
                        messId,
                        now
                );

        MealPricing existingSchedule = null;

        if (scheduled) {

            if (upcomingPricing.size() > 1) {
                throw new BusinessException(
                        "Multiple upcoming price changes already exist. "
                                + "Cancel the extra entries before updating pricing."
                );
            }

            if (!upcomingPricing.isEmpty()) {

                existingSchedule = upcomingPricing.get(0);

                if (!existingSchedule.getEffectiveFrom()
                        .equals(effectiveFrom)) {

                    throw new BusinessException(
                            "A price change is already planned for "
                                    + formatDate(
                                            existingSchedule
                                                    .getEffectiveFrom()
                                                    .toLocalDate()
                                    )
                                    + ". Update that change or cancel it "
                                    + "before choosing a different date."
                    );
                }
            }
        }

        MealPricing comparisonPricing = existingSchedule != null
                ? existingSchedule
                : mealPricingRepository
                        .findTopByMess_MessIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                                messId,
                                effectiveFrom
                        )
                        .orElse(null);

        /*
         * Identical requests do not create another version or send
         * another notification.
         */
        if (hasSamePrices(comparisonPricing, request)) {
            return mealPricingMapper.toResponse(comparisonPricing);
        }

        /*
         * Immediate changes remain available. A future change is
         * preserved and will still take effect on its announced date.
         */
        if (!scheduled
                && mealPricingRepository
                        .findByMess_MessIdAndEffectiveFrom(
                                messId,
                                effectiveFrom
                        )
                        .isPresent()) {

            throw new BusinessException(
                    "Another pricing update just completed. Please retry."
            );
        }

        MealPricing pricing = existingSchedule != null
                ? existingSchedule
                : new MealPricing();

        pricing.setMess(mess);
        pricing.setEffectiveFrom(effectiveFrom);
        pricing.setHalfMealPrice(request.halfMealPrice());
        pricing.setFullMealPrice(request.fullMealPrice());
        pricing.setExtraRotiPrice(request.extraRotiPrice());

        MealPricing savedPricing =
                mealPricingRepository.saveAndFlush(pricing);

        String message;

        if (scheduled) {
            message = (existingSchedule == null
                    ? "New meal prices will apply from "
                    : "The meal prices announced for ")
                    + formatDate(effectiveDate)
                    + (existingSchedule == null
                            ? ". "
                            : " have been revised. ")
                    + buildPricingNotificationMessage(savedPricing);
        } else {
            message = "Meal prices have been updated and apply "
                    + "from now, "
                    + formatDate(today)
                    + ". "
                    + buildPricingNotificationMessage(savedPricing);
        }

        notificationService.notifyActiveCustomers(
                NotificationType.MEAL_PRICING,
                "Meal Price Update",
                message
        );

        return mealPricingMapper.toResponse(savedPricing);
    }

    @Transactional
    @Override
    public void cancelScheduledPricing(Long mealPricingId) {

        requireOwner();

        if (mealPricingId == null || mealPricingId <= 0) {
            throw new BusinessException(
                    "Meal pricing ID must be positive."
            );
        }

        Long messId = customerSecurity.getCurrentMessId();

        lockMess(messId);

        /*
         * Capture time after obtaining the lock. An entry that has
         * already taken effect cannot be cancelled.
         */
        LocalDateTime now = LocalDateTime.now(clock)
                .truncatedTo(ChronoUnit.MICROS);

        MealPricing pricing = mealPricingRepository
                .findByMealPricingIdAndMess_MessId(
                        mealPricingId,
                        messId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Upcoming price change not found."
                ));

        if (!pricing.getEffectiveFrom().isAfter(now)) {
            throw new BusinessException(
                    "This pricing has already taken effect and cannot "
                            + "be cancelled. Create a new price update instead."
            );
        }

        String effectiveDate = formatDate(
                pricing.getEffectiveFrom().toLocalDate()
        );

        mealPricingRepository.delete(pricing);
        mealPricingRepository.flush();

        /*
         * Deletion and notification persistence share this
         * transaction.
         */
        notificationService.notifyActiveCustomers(
                NotificationType.MEAL_PRICING,
                "Meal Price Change Cancelled",
                "The meal price change announced for "
                        + effectiveDate
                        + " has been cancelled. "
                        + "That change will no longer apply."
        );
    }

    private Mess lockMess(Long messId) {

        Mess mess = entityManager.find(
                Mess.class,
                messId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (mess == null) {
            throw new ResourceNotFoundException(
                    "Mess not found."
            );
        }

        return mess;
    }

    private boolean hasSamePrices(
            MealPricing pricing,
            UpdateMealPricingRequest request) {

        return pricing != null
                && samePrice(
                        pricing.getHalfMealPrice(),
                        request.halfMealPrice()
                )
                && samePrice(
                        pricing.getFullMealPrice(),
                        request.fullMealPrice()
                )
                && samePrice(
                        pricing.getExtraRotiPrice(),
                        request.extraRotiPrice()
                );
    }

    private boolean samePrice(
            BigDecimal current,
            BigDecimal requested) {

        return current != null
                && current.compareTo(requested) == 0;
    }

    private void validatePrice(
            BigDecimal price,
            String label) {

        if (price == null
                || price.signum() <= 0
                || price.compareTo(MAX_PRICE) > 0
                || price.stripTrailingZeros().scale() > 2) {

            throw new BusinessException(
                    label
                            + " price must be greater than 0, "
                            + "with at most 8 whole digits and "
                            + "2 decimal places."
            );
        }
    }

    private void requireOwner() {

        if (customerSecurity.getCurrentUserRole() != UserRole.OWNER) {
            throw new AccessDeniedException(
                    "Only mess owners can manage meal pricing."
            );
        }
    }

    private String buildPricingNotificationMessage(
            MealPricing pricing) {

        return "Full meal: ₹"
                + formatPrice(pricing.getFullMealPrice())
                + " · Half meal: ₹"
                + formatPrice(pricing.getHalfMealPrice())
                + " · Extra roti: ₹"
                + formatPrice(pricing.getExtraRotiPrice())
                + ".";
    }

    private String formatPrice(BigDecimal price) {

        return price.stripTrailingZeros().toPlainString();
    }

    private String formatDate(LocalDate date) {

        return DATE_FORMAT.format(date);
    }
}