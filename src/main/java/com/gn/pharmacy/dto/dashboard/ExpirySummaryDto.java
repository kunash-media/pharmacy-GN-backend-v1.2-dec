package com.gn.pharmacy.dto.dashboard;

import java.time.LocalDate;
import java.util.List;

public record ExpirySummaryDto(
        long within30Days,
        long within60Days,
        long within90Days,
        List<ExpiryItemDto> items
) {}

