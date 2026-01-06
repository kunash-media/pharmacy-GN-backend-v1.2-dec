package com.gn.pharmacy.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyRevenueDto(
        List<String> months,
        List<BigDecimal> revenues
) {}