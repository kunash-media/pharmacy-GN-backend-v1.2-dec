package com.gn.pharmacy.dto.dashboard;

import java.time.LocalDateTime;

public record RecentPrescriptionDto(
        String patientName,
        String prescriptionId,
        LocalDateTime date,
        String status
) {}