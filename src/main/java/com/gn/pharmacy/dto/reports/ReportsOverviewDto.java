package com.gn.pharmacy.dto.reports;

import java.math.BigDecimal;

public record ReportsOverviewDto(
        BigDecimal totalRevenue,
        long totalOrders,
        long totalProducts,
        BigDecimal totalStockValue
) {}