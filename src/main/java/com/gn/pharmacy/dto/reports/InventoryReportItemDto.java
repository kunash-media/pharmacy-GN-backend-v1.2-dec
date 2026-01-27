package com.gn.pharmacy.dto.reports;

import java.math.BigDecimal;

public record InventoryReportItemDto(
        Long inventoryId,
        String productName,
        String category,
        String subcategory,
        Integer currentStock,
        BigDecimal stockValue,
        String expiryDate,
        String batchNo,
        String stockStatus
) {}
