package org.teamseven.hms.backend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.teamseven.hms.backend.shared.ResponseWrapper;

import java.util.UUID;
import java.util.logging.Logger;

@Service
@EnableConfigurationProperties(CatalogClientConfig.class)
public class CatalogClient {
    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private CatalogClientConfig config;

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ServiceOverview getServiceOverview(UUID serviceId) {
        TypeReference<ResponseWrapper.Success<ServiceOverview>> typeReference = new TypeReference<>() {};
        return executeGetRequest(
                config.getBaseUrl() + config.getOverviewPath().replace("{service-id}", serviceId.toString()),
                typeReference
        );
    }

    public ServiceOverview getServiceOverviewByDoctorId(UUID doctorId) {
        TypeReference<ResponseWrapper.Success<ServiceOverview>> typeReference = new TypeReference<>() {};
        return executeGetRequest(
                config.getBaseUrl() + config.getOverviewByDoctorIdPath().replace("{doctor-id}", doctorId.toString()),
                typeReference
        );
    }

    private<T> T executeGetRequest(
            String getUrl,
            TypeReference<ResponseWrapper.Success<T>> valueTypeRefResponse
    ) {
        try {
            Request request = new Request.Builder()
                    .url(getUrl)
                    .get()
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;

            return MAPPER.readValue(response.body().string(), valueTypeRefResponse).getData();
        } catch (Exception e) {
            Logger.getAnonymousLogger().info("caught exception " + e);
            throw new RuntimeException("Internal server error.");
        }
    }

    public UUID createNewService(CreateDoctorService createDoctorService) {
        String url = config.getBaseUrl() + config.getCreateDoctorPath();
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MAPPER.writeValueAsString(createDoctorService), JSON))
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;

            return MAPPER.readValue(response.body().string(), new TypeReference<ResponseWrapper.Success<UUID>>() {})
                    .getData();
        } catch (Exception e) {
            Logger.getAnonymousLogger().info("caught exception " + e);
            throw new RuntimeException("Internal server error.");
        }
    }
}
