package com.gn.pharmacy.dto.reports;

import java.math.BigDecimal;
import java.util.List;

public record PagedSalesReportDto(
        List<SalesReportItemDto> items,
        long totalItems,
        int page,
        int limit,
        BigDecimal totalRevenue,
        String topProduct,
        long totalOrders,
        BigDecimal totalProfit
) {}