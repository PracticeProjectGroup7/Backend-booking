package org.teamseven.hms.backend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.teamseven.hms.backend.shared.ResponseWrapper;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@EnableConfigurationProperties(UserClientConfig.class)
public class UserClient {
    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private UserClientConfig config;

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public PatientProfileOverview getPatientProfile(UUID patientId) {
        TypeReference<ResponseWrapper.Success<PatientProfileOverview>> typeReference = new TypeReference<>() {};
        String getUrl = config.getBaseUrl() + config.getGetPatientByIdPath().replace("{patient-id}", patientId.toString());
        try {
            Request request = new Request.Builder()
                    .url(getUrl)
                    .get()
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;

            return MAPPER.readValue(response.body().string(), typeReference).getData();
        } catch (Exception e) {
            Logger.getAnonymousLogger().info("caught exception " + e);
            throw new RuntimeException("Internal server error.");
        }
    }

    public List<PatientProfileOverview> getByUUIDs(List<UUID> uuids) {
        try {
            RequestBody body = RequestBody.create(MAPPER.writeValueAsString(uuids), JSON);

            Request request = new Request.Builder()
                    .url(config.getBaseUrl() + config.getPatientByUuidsPath())
                    .method(RequestMethod.POST.toString(), body)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;
            ResponseWrapper.Success<List<PatientProfileOverview>> profiles = MAPPER.readValue(
                    response.body().string(),
                    new TypeReference<>() {}
            );

            return profiles.getData();
        } catch (Exception e) {
            Logger.getAnonymousLogger().info("caught exception " + e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
