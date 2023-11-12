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
public class ServiceOverview {
    private UUID serviceId;
    private UUID staffId;
    private UUID doctorId;
    private String type;
    private String name;
    private String description;
    private Double estimatedPrice;
}
