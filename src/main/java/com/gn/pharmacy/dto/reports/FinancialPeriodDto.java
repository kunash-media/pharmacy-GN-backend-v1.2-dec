package com.gn.pharmacy.dto.reports;

import java.math.BigDecimal;

public record FinancialPeriodDto(
        String period,
        BigDecimal revenue,
        BigDecimal expenses,
        BigDecimal profit
) {}