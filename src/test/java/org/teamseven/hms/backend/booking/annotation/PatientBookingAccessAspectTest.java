package org.teamseven.hms.backend.booking.annotation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
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
import org.teamseven.hms.backend.booking.controller.BookingController;
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
public class PatientBookingAccessAspectTest {
    @Mock
    private JwtService jwtService;

    @InjectMocks
    PatientBookingAccessAspect aspect;

    @BeforeEach
    public void setUp() {
        Mockito.reset(jwtService);
    }

    @Test
    public void testValidateAccessAllowed_jwtTokenMissing_throwsUnauthorizedRequest() {
        ProceedingJoinPoint proceedingJoinPoint = mock();
        PatientBookingAccessValidated patientBookingAccessValidated = mock();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThrows(
                UnauthorizedAccessException.class,
                () -> { aspect.validateAccessAllowed(proceedingJoinPoint, patientBookingAccessValidated); }
        );
    }

    @Test
    public void testValidateAccessAllowed_userIsDoctor_assertNotThrowUnauthorized() {
        ProceedingJoinPoint proceedingJoinPoint = mock();
        PatientBookingAccessValidated patientBookingAccessValidated = mock();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.addHeader("Authorization", "Bearer mock token");

        when(jwtService.extractAllClaims(any()))
                .thenReturn(
                        getMockClaims(Role.DOCTOR, UUID.randomUUID().toString())
                );

        assertDoesNotThrow(
                () -> { aspect.validateAccessAllowed(proceedingJoinPoint, patientBookingAccessValidated); }
        );
    }

    @Test
    public void testValidateAccessAllowed_userIsPatient_accessingByPatientId_whenAuthorizedThenSuccess() throws Exception {
        ProceedingJoinPoint proceedingJoinPoint = mock();
        JoinPoint.StaticPart staticPartJoinPoint = mock();
        MethodSignature methodSignature = mock();
        PatientBookingAccessValidated patientBookingAccessValidated = mock();

        mockPatientAccessSetUp(patientBookingAccessValidated, methodSignature, staticPartJoinPoint);

        when(proceedingJoinPoint.getStaticPart()).thenReturn(staticPartJoinPoint);
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"166b8eb1-5ea1-408a-b0d2-fe4ee88f57eb", 1, 10});

        assertDoesNotThrow(
                () -> { aspect.validateAccessAllowed(proceedingJoinPoint, patientBookingAccessValidated); }
        );
    }

    @Test
    public void testValidateAccessAllowed_userIsPatient_accessingByPatientId_whenunauthorizedThenThrowException()
            throws Exception {
        ProceedingJoinPoint proceedingJoinPoint = mock();
        JoinPoint.StaticPart staticPartJoinPoint = mock();
        MethodSignature methodSignature = mock();
        PatientBookingAccessValidated patientBookingAccessValidated = mock();

        mockPatientAccessSetUp(patientBookingAccessValidated, methodSignature, staticPartJoinPoint);

        when(proceedingJoinPoint.getStaticPart()).thenReturn(staticPartJoinPoint);
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"ae3f7eaf-185e-4487-b9a0-0001b6ac057f", 1, 10});

        assertThrows(
                UnauthorizedAccessException.class,
                () -> aspect.validateAccessAllowed(proceedingJoinPoint, patientBookingAccessValidated)
        );
    }

    private void mockPatientAccessSetUp(
            PatientBookingAccessValidated patientBookingAccessValidated,
            MethodSignature methodSignature,
            JoinPoint.StaticPart staticPartJoinPoint
    ) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.addHeader("Authorization", "Bearer mock token");

        when(jwtService.extractAllClaims(any())).thenReturn(
                getMockClaims(Role.PATIENT, "166b8eb1-5ea1-408a-b0d2-fe4ee88f57eb")
        );

        when(patientBookingAccessValidated.dataRequestMethod()).thenReturn(PatientDataRequestedMethod.PATIENT_ID_PATH);
        when(methodSignature.getMethod()).thenReturn(
                BookingController.class.getMethod("getBookingHistory", UUID.class, int.class, int.class)
        );

        when(staticPartJoinPoint.getSignature()).thenReturn(methodSignature);
    }

    private Claims getMockClaims(
            Role role,
            String roleId
    ) {
        byte[] randomKeyBytes = "c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2".getBytes();
        Key mockKey = Keys.hmacShaKeyFor(randomKeyBytes);
        String jwt = Jwts.builder()
                .setClaims(
                        Map.of("ROLE", role, "roleId", roleId)
                )
                .signWith(mockKey, SignatureAlgorithm.HS256)
                .compact();
        System.out.println("jwt " + jwt);
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(mockKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        System.out.println("claims " + claims.entrySet());
        return claims;
    }
}
