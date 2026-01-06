package com.gn.pharmacy.dto.dashboard;

public record LowStockDto(
        String productName,
        String sku,
        int currentStock,
        String alertLevel
) {}
