package org.teamseven.hms.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrieveStaffItem {
    private UUID staffId;
    private UUID userId;
    private String firstname;
    private String lastName;
    private String email;
}
