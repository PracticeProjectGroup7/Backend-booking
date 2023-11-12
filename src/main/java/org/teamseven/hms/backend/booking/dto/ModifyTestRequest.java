package org.teamseven.hms.backend.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModifyTestRequest {
    private String patientId;
    private String serviceId;
    private String oldReservedDate;
    private String newReservedDate;
}
