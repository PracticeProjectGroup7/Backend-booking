package org.teamseven.hms.backend.client;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {
    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient();
    }
}
