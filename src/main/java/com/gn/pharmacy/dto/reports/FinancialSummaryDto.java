package com.gn.pharmacy.dto.reports;

import java.math.BigDecimal;
import java.util.List;

public record FinancialSummaryDto(
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal netProfit,
        double profitMargin,
        List<FinancialPeriodDto> breakdown
) {}