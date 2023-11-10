package org.teamseven.hms.backend.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.teamseven.hms.backend.booking.entity.Booking;
import org.teamseven.hms.backend.booking.entity.TestRepository;
import org.teamseven.hms.backend.booking.entity.TestStatus;
import org.teamseven.hms.backend.client.PatientProfileOverview;
import org.teamseven.hms.backend.client.UserClient;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LabTestServiceTest {
    @Mock
    private TestRepository testRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private LabTestService labTestService;

    @Test
    public void testGetTestAppointments_givenFoundTests_returnList() throws ParseException {
        when(bookingService.findUpcomingTestBookings(any()))
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

        labTestService.getTestAppointments(1, 10);
        verify(bookingService).findUpcomingTestBookings(any());
        verify(userClient).getByUUIDs(List.of(UUID.fromString("8a61e51b-7930-4549-b273-a9143abde3e3")));
    }

    private List<Booking> getMockBookingList() throws ParseException {
        return List.of(
                Booking.builder()
                        .test(
                                org.teamseven.hms.backend.booking.entity.Test.builder()
                                        .status(TestStatus.PENDING)
                                                .testName("test name")
                                        .build()
                        )
                        .patientId(UUID.fromString("8a61e51b-7930-4549-b273-a9143abde3e3"))
                        .bookingId(UUID.fromString("efb5f4bf-75e3-49a5-8785-bb82b70029ed"))
                        .serviceId(UUID.fromString("c4dcd184-ae0b-4cd9-bd91-22ff4ce71321"))
                        .reservedDate("2023-12-11")
                        .build()
        );
    }
}
