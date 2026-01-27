package com.gn.pharmacy.dto.reports;

import java.math.BigDecimal;

public record CustomerReportItemDto(
        Long customerId,
        String customerName,
        long totalOrders,
        BigDecimal totalRevenue,
        java.util.Date lastOrderDate,
        String topCategory
) {}