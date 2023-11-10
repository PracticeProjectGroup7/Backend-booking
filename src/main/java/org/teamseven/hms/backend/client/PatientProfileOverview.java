package org.teamseven.hms.backend.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileOverview {
    private String patientName;
    private String gender;
    private String dateOfBirth;
    private UUID patientId;
}