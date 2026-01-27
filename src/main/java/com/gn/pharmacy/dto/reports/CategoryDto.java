package com.gn.pharmacy.dto.reports;

import java.util.List;

public record CategoryDto(
        String name,
        List<String> subcategories
) {}
