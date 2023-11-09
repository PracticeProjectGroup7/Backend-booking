package org.teamseven.hms.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.util.logging.Logger;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityEnvConfig.class)
public class SecurityConfiguration {
    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",
            "/api/v1/available-services/*",
            "/api/v1/samples",
            "/api/v1/doctors"
    };
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final SecurityEnvConfig envConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        if (envConfig.isEnableLoginGuard()) {
            Logger.getAnonymousLogger().warning("Log in guard activated. configuring filter chain..");
            http.authorizeHttpRequests(req ->
                            req.requestMatchers(WHITE_LIST_URL)
                                    .permitAll()
                                    .anyRequest()
                                    .authenticated()
                    )
                    .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                    .authenticationProvider(authenticationProvider)
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            Logger.getAnonymousLogger().warning("Disabling log in security filter chain..");
        }

        return http.build();
    }
}
