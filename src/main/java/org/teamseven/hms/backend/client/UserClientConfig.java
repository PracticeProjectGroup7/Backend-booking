package org.teamseven.hms.backend.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "client.user", ignoreUnknownFields = false)
public class UserClientConfig {
    private String baseUrl = "";
    private String getPatientByIdPath = "";
    private String patientByUuidsPath = "";
}
