package org.teamseven.hms.backend.booking.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.teamseven.hms.backend.booking.entity.Appointment;
import org.teamseven.hms.backend.booking.entity.AppointmentStatus;
import org.teamseven.hms.backend.booking.entity.Booking;
import org.teamseven.hms.backend.client.CatalogClient;
import org.teamseven.hms.backend.client.PatientProfileOverview;
import org.teamseven.hms.backend.client.ServiceOverview;
import org.teamseven.hms.backend.client.UserClient;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {
    @Mock
    private CatalogClient catalogClient;

    @Mock
    private BookingService bookingService;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    public void getDoctorSlots_appointmentsExist_returnAppointmentInfo() throws Exception{
        HttpServletRequest request = mock();

        when(request.getAttribute("roleId")).thenReturn("00b063be-5002-40a8-9073-54026c0615b2");

        UUID mockServiceId = UUID.fromString("5cd46763-5869-4dec-bec6-588c55398bff");
        when(catalogClient.getServiceOverviewByDoctorId(any()))
                .thenReturn(
                        ServiceOverview.builder().serviceId(mockServiceId).build()
                );

        when(bookingService.findUpcomingServiceBookings(any(), any()))
                .thenReturn(
                        new PageImpl(
                                getMockBookingList(),
                                PageRequest.of(0, 10),
                                1L
                        )
                );

        when(userClient.getByUUIDs(any())).thenReturn(
                List.of(
                        PatientProfileOverview.builder()
                                .patientName("test name")
                                .patientId(UUID.fromString("8a61e51b-7930-4549-b273-a9143abde3e3"))
                                .build()
                )
        );

        appointmentService.getDoctorSlots(request, 1, 10);
        verify(catalogClient).getServiceOverviewByDoctorId(UUID.fromString("00b063be-5002-40a8-9073-54026c0615b2"));
        verify(bookingService).findUpcomingServiceBookings(eq(mockServiceId), any());
        verify(userClient).getByUUIDs(List.of(UUID.fromString("8a61e51b-7930-4549-b273-a9143abde3e3")));
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
                .slots("1")
                .patientId(UUID.fromString("8a61e51b-7930-4549-b273-a9143abde3e3"))
                .bookingId(UUID.fromString("efb5f4bf-75e3-49a5-8785-bb82b70029ed"))
                .serviceId(UUID.fromString("c4dcd184-ae0b-4cd9-bd91-22ff4ce71321"))
                .reservedDate("2023-12-11")
                .build();
    }

}
