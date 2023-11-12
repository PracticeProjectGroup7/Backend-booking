package org.teamseven.hms.backend.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModifyBookingRequest {
    private String patientId;
    private String serviceId;
    private String oldReservedDate;
    private String newReservedDate;
    private String oldSlot;
    private String newSlot;
}
