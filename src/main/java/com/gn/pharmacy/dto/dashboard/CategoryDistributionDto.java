package com.gn.pharmacy.dto.dashboard;

import java.util.List;

public record CategoryDistributionDto(
        List<String> categories,
        List<Long> counts
) {}