package org.teamseven.hms.backend.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "client.catalog", ignoreUnknownFields = false)
public class CatalogClientConfig {
    private String baseUrl = "";
    private String overviewPath = "";
    private String overviewByDoctorIdPath = "";
    private String createDoctorPath = "";
}
