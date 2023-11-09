package org.teamseven.hms.backend.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDoctorService {
    private String serviceId;
    private UUID doctorId;
    private String name;
    private String description;
    private Double estimatedPrice;
}
