package org.teamseven.hms.backend.booking.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminEndpointAccessValidated {
    PatientDataRequestedMethod dataRequestMethod() default PatientDataRequestedMethod.PATIENT_ID_PATH;
    boolean isReceptionistAccessAllowed() default false;
}
