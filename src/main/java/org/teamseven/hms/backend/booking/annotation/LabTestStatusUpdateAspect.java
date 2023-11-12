package org.teamseven.hms.backend.booking.annotation;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.teamseven.hms.backend.config.JwtService;
import org.teamseven.hms.backend.shared.exception.UnauthorizedAccessException;

@Aspect
@Component
public class LabTestStatusUpdateAspect {
    @Autowired private JwtService jwtService;

    @VisibleForTesting
    @Around("@annotation(labTestEndpointAccessValidated)")
    protected Object validateAccessAllowed(
            ProceedingJoinPoint pjp,
            LabTestEndpointAccessValidated labTestEndpointAccessValidated
    ) throws Throwable {
        HttpServletRequest request = (
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()
        ).getRequest();
        String jwtToken = getJwtToken(request);
        Claims jwtClaims = jwtService.extractAllClaims(jwtToken);
        Role role = Role.valueOf(jwtClaims.get("ROLE", String.class));

        if (role == Role.LAB_SUPPORT_STAFF) {
            return pjp.proceed();
        }
        throw new UnauthorizedAccessException();
    }

    private String getJwtToken(HttpServletRequest request) {
        try {
            return request.getHeader("Authorization").substring(7);
        } catch (Exception e) {
            throw new UnauthorizedAccessException();
        }
    }
}
