package org.teamseven.hms.backend.booking.annotation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.teamseven.hms.backend.config.JwtService;
import org.teamseven.hms.backend.shared.exception.UnauthorizedAccessException;

import java.security.Key;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DoctorAppointmentEndpointAspectTest {
    @Mock
    private JwtService jwtService;

    @InjectMocks
    DoctorAppointmentEndpointAccessAspect aspect;

    @BeforeEach
    public void setUp() {
        Mockito.reset(jwtService);
    }

    @Test
    public void testValidateAccessAllowed_userIsDoctor_assertNotThrowUnauthorized() {
        ProceedingJoinPoint proceedingJoinPoint = mock();
        DoctorAppointmentEndpointAccessValidated mockAnnotation = mock();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.addHeader("Authorization", "Bearer mock token");

        when(jwtService.extractAllClaims(any()))
                .thenReturn(
                        getMockClaims(Role.DOCTOR, UUID.randomUUID().toString())
                );

        assertDoesNotThrow(
                () -> { aspect.validateAccessAllowed(proceedingJoinPoint, mockAnnotation); }
        );
    }

    @Test
    public void testValidateAccessAllowed_userIsNotDoctor_assertThrowUnauthorized() {
        ProceedingJoinPoint proceedingJoinPoint = mock();
        DoctorAppointmentEndpointAccessValidated mockAnnotation = mock();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.addHeader("Authorization", "Bearer mock token");

        when(jwtService.extractAllClaims(any()))
                .thenReturn(
                        getMockClaims(Role.LAB_SUPPORT_STAFF, UUID.randomUUID().toString())
                );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> { aspect.validateAccessAllowed(proceedingJoinPoint, mockAnnotation); }
        );
    }

    private Claims getMockClaims(
            Role role,
            String roleId
    ) {
        byte[] randomKeyBytes = "c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2".getBytes();
        Key mockKey = Keys.hmacShaKeyFor(randomKeyBytes);
        String jwt = Jwts.builder()
                .setClaims(Map.of("ROLE", role, "roleId", roleId))
                .signWith(mockKey, SignatureAlgorithm.HS256)
                .compact();
        return Jwts
                .parserBuilder()
                .setSigningKey(mockKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
}
