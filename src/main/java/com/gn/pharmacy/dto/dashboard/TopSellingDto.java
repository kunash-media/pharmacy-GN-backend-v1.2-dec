package com.gn.pharmacy.dto.dashboard;

import java.math.BigDecimal;

public record TopSellingDto(
        String productName,
        long unitsSold,
        BigDecimal revenue
) {}