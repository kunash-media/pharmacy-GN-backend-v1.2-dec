package com.gn.pharmacy.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryDto(
        BigDecimal totalProfit,
        long totalPrescriptions,
        long totalInventoryItems,
        long lowStockItems,
        String profitTrend,
        double profitChangePercent
) {}