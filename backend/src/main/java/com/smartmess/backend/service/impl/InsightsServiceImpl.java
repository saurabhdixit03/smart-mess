package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.response.CustomerInsightsResponse;
import com.smartmess.backend.dto.response.FinancialInsightsResponse;
import com.smartmess.backend.dto.response.MealInsightsResponse;
import com.smartmess.backend.dto.response.MonthlyInsightsResponse;
import com.smartmess.backend.repository.BillRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.service.InsightsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InsightsServiceImpl
        implements InsightsService {

    private final BillRepository billRepository;

    private final MealRecordRepository mealRecordRepository;

    @Override
    public MonthlyInsightsResponse getMonthlyInsights(
            Integer month,
            Integer year) {

        List<Object[]> financialRows =
                billRepository.getMonthlyFinancialInsights(month, year);

        List<Object[]> mealRows =
                mealRecordRepository.getMonthlyMealInsights(month, year);

        if (financialRows.isEmpty()) {
            return new MonthlyInsightsResponse(
                    Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    year,
                    new FinancialInsightsResponse(
                            0L,
                            0L,
                            0L,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            0.0
                    ),
                    new CustomerInsightsResponse(0L),
                    new MealInsightsResponse(
                            0L,
                            0L,
                            0L,
                            0L,
                            0L
                    )
            );
        }

        Object[] financialData = financialRows.get(0);

        Object[] mealData =
                mealRows.isEmpty()
                        ? new Object[] {0L, 0L, 0L, 0L, 0L}
                        : mealRows.get(0);

        CustomerInsightsResponse customerInsights =
                new CustomerInsightsResponse(
                        getLong(financialData[0])
                );

        Long billsGenerated = getLong(financialData[1]);
        Long paidBills = getLong(financialData[2]);
        Long pendingBills = getLong(financialData[3]);

        BigDecimal totalRevenue = getBigDecimal(financialData[4]);
        BigDecimal collectedRevenue = getBigDecimal(financialData[5]);
        BigDecimal pendingRevenue = getBigDecimal(financialData[6]);

        double collectionRate = 0.0;

        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate =
                    collectedRevenue
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalRevenue, 2, java.math.RoundingMode.HALF_UP)
                            .doubleValue();
        }

        FinancialInsightsResponse financialInsights =
                new FinancialInsightsResponse(
                        billsGenerated,
                        paidBills,
                        pendingBills,
                        totalRevenue,
                        collectedRevenue,
                        pendingRevenue,
                        collectionRate
                );

        MealInsightsResponse mealInsights =
                new MealInsightsResponse(
                        getLong(mealData[0]),
                        getLong(mealData[1]),
                        getLong(mealData[2]),
                        getLong(mealData[3]),
                        getLong(mealData[4])
                );

        return new MonthlyInsightsResponse(
                Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                year,
                financialInsights,
                customerInsights,
                mealInsights
        );
    }
    
    private Long getLong(Object value) {

        return value == null
                ? 0L
                : ((Number) value).longValue();

    }

    private BigDecimal getBigDecimal(Object value) {

        return value == null
                ? BigDecimal.ZERO
                : (BigDecimal) value;

    }
    

}