package com.gn.pharmacy.dto.reports;

public record SalesReportItemDto(
        Long orderItemId,
        String productName,
        String category,
        String subcategory,
        Integer quantity,
        Double revenue,
        String saleDate,
        String orderStatus,
        String customerName
) {}
