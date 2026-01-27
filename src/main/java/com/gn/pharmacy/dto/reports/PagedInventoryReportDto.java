package com.gn.pharmacy.dto.reports;

import java.math.BigDecimal;
import java.util.List;

public record PagedInventoryReportDto(
        List<InventoryReportItemDto> items,
        long totalItems,
        int page,
        int limit,
        BigDecimal totalStockValue,
        long lowStockCount,
        long expiringSoonCount,
        long totalProducts
) {}