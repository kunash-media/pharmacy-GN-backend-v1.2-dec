package com.gn.pharmacy.dto.dashboard;

import java.time.LocalDate;

public record ExpiryItemDto(
        String productName,
        LocalDate expiryDate,
        String period
) {}
