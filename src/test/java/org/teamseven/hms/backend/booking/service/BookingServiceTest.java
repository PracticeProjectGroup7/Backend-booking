package org.teamseven.hms.backend.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.teamseven.hms.backend.booking.dto.*;
import org.teamseven.hms.backend.booking.entity.*;
import org.teamseven.hms.backend.client.CatalogClient;
import org.teamseven.hms.backend.client.ServiceOverview;
import org.teamseven.hms.backend.user.User;
import org.teamseven.hms.backend.user.dto.PatientProfileOverview;
import org.teamseven.hms.backend.user.entity.Patient;
import org.teamseven.hms.backend.user.entity.PatientRepository;
import org.teamseven.hms.backend.user.service.PatientService;

import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private BookingService bookingService;


    @Test
    public void testGetBookingHistory_patientNotFound_assertThrowsException() {
        when(patientRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            bookingService.getBookingHistory(UUID.randomUUID(), 1, 10);
        });
    }

    @Test
    public void testGetBookingHistory_patientFound_returnFormattedDataFromRepository() throws ParseException {
        when(patientRepository.findById(any()))
                .thenReturn(
                        Optional.of(
                                Patient.builder().user(
                                        User.builder().firstName("John").lastName("Doe").build()
                                ).build()
                        )
                );

        when(bookingRepository.findPatientBookingsWithPagination(any(), any()))
                        .thenReturn(
                                new PageImpl(
                                        getMockBookingList(),
                                        PageRequest.of(0, 10),
                                        1L
                                )
                        );

        when(catalogClient.getServiceOverview(any()))
                .thenReturn(
                        ServiceOverview.builder()
                                .serviceId(UUID.randomUUID())
                                .name("Loki")
                                .type("APPOINTMENT")
                                .description("test description")
                                .build()
                );

        BookingPaginationResponse response = bookingService.getBookingHistory(UUID.randomUUID(), 1, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals("John Doe", response.getPatientName());
        assertEquals(1, response.getBookingList().size());

        BookingOverview overview = response.getBookingList().get(0);
        assertEquals(UUID.fromString("efb5f4bf-75e3-49a5-8785-bb82b70029ed"), overview.getBookingId());
        assertEquals(BookingType.APPOINTMENT, overview.getType());
        assertEquals("Appointment with Dr. Loki", overview.getBookingDescription());
    }

    private List<Booking> getMockBookingList() throws ParseException {
        return List.of(getAppointmentBooking());
    }

    private Booking getAppointmentBooking() throws ParseException {
        return Booking.builder()
                .appointment(
                        Appointment.builder()
                                .status(AppointmentStatus.PENDING)
                                .diagnosis("test").
                                appointmentId(UUID.randomUUID())
                                .build()
                )
                .slots("1,2")
                .bookingId(UUID.fromString("efb5f4bf-75e3-49a5-8785-bb82b70029ed"))
                .serviceId(UUID.fromString("c4dcd184-ae0b-4cd9-bd91-22ff4ce71321"))
                .reservedDate("2023-12-11")
                .build();
    }

    @Test
    public void testGetBookingInfo_bookingNotFound_throwsNoSuchElement() {
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());

        UUID uuid = UUID.randomUUID();

        assertThrows(
                NoSuchElementException.class,
                () -> bookingService.getBookingById(uuid)
        );

        verify(bookingRepository).findById(uuid);
    }

    @Test
    public void testGetBookingInfo_appointmentBookingFound_returnBookingDetails() throws ParseException {
        when(bookingRepository.findById(any())).
                thenReturn(Optional.of(getAppointmentBooking()));

        when(patientService.getPatientProfile(any()))
                .thenReturn(
                        PatientProfileOverview.builder()
                                .patientName("John Doe")
                                .gender("M")
                                .dateOfBirth("1990-01-01")
                                .build()
                );

        when(catalogClient.getServiceOverview(any()))
                .thenReturn(
                        ServiceOverview.builder()
                                .name("Dr. Loki")
                                .description("Dermatology")
                                .type("APPOINTMENT")
                                .build()
                );

        UUID uuid = UUID.randomUUID();

        BookingInfoResponse response = bookingService.getBookingInfo(uuid);

        verify(bookingRepository).findById(uuid);
        verify(catalogClient).getServiceOverview(UUID.fromString("c4dcd184-ae0b-4cd9-bd91-22ff4ce71321"));
        assertTrue(response.getDetails() instanceof BookingDetails.Appointment);
        BookingDetails.Appointment details = (BookingDetails.Appointment) response.getDetails();
        assertEquals("Dr. Loki", details.getDoctorName());
        assertEquals("Dermatology", details.getDepartment());
        assertEquals("test", details.getComments());
    }

    @Test
    public void testGetBookingInfo_testBookingFound_returnBookingDetails() throws ParseException {
        when(bookingRepository.findById(any())).
                thenReturn(Optional.of(getTestBooking()));

        when(catalogClient.getServiceOverview(any()))
                .thenReturn(
                        ServiceOverview.builder()
                                .name("Lab test")
                                .type("TEST")
                                .build()
                );

        when(patientService.getPatientProfile(any()))
                .thenReturn(
                        PatientProfileOverview.builder()
                                .patientName("John Doe")
                                .gender("M")
                                .dateOfBirth("1990-01-01")
                                .build()
                );

        UUID uuid = UUID.randomUUID();

        BookingInfoResponse response = bookingService.getBookingInfo(uuid);

        verify(bookingRepository).findById(uuid);
        verify(catalogClient).getServiceOverview(UUID.fromString("c4dcd184-ae0b-4cd9-bd91-22ff4ce71321"));
        assertTrue(response.getDetails() instanceof BookingDetails.Test);
        BookingDetails.Test details = (BookingDetails.Test) response.getDetails();
        assertEquals("Lab test", details.getTestName());
        assertEquals("report", details.getTestResult());
    }

    private Booking getTestBooking() throws ParseException {
        return Booking.builder()
                .test(
                        org.teamseven.hms.backend.booking.entity.Test.builder()
                        .testReport("report")
                                .status(TestStatus.PENDING)
                        .testId(UUID.randomUUID())
                        .build()
                )
                .slots("1,2")
                .bookingId(UUID.fromString("efb5f4bf-75e3-49a5-8785-bb82b70029ed"))
                .serviceId(UUID.fromString("c4dcd184-ae0b-4cd9-bd91-22ff4ce71321"))
                .reservedDate("2023-12-11")
                .build();
    }
}
