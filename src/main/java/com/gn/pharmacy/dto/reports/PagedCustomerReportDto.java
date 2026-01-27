package com.gn.pharmacy.dto.reports;

import java.math.BigDecimal;
import java.util.List;

public record PagedCustomerReportDto(
        List<CustomerReportItemDto> items,
        long totalItems,
        int page,
        int limit,
        long totalUniqueCustomers,
        String topCustomer,
        BigDecimal avgOrderValue
) {}