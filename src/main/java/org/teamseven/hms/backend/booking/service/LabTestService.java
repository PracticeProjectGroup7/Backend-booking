package org.teamseven.hms.backend.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.teamseven.hms.backend.booking.dto.TestAppointmentSlotItem;
import org.teamseven.hms.backend.booking.dto.TestAppointmentSlotPaginationResponse;
import org.teamseven.hms.backend.booking.entity.Booking;
import org.teamseven.hms.backend.booking.entity.TestRepository;
import org.teamseven.hms.backend.booking.entity.TestStatus;
import org.teamseven.hms.backend.client.PatientProfileOverview;
import org.teamseven.hms.backend.client.UserClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LabTestService {
    @Autowired private TestRepository testRepository;

    @Autowired private BookingService bookingService;

    @Autowired private UserClient userClient;

    public boolean updateTestStatus(UUID testId, String result, TestStatus newStatus) {
        return testRepository.setTestStatus(newStatus, result,  testId) == 1;
    }

    public TestAppointmentSlotPaginationResponse getTestAppointments(int page, int pageSize) {
        int zeroBasedIdxPage = page - 1;
        Page<Booking> bookings = bookingService.findUpcomingTestBookings(
                Pageable.ofSize(pageSize).withPage(zeroBasedIdxPage)
        );

        List<UUID> patientIdList = bookings.getContent()
                .stream().map(Booking::getPatientId).toList();

        Map<UUID, PatientProfileOverview> patientProfiles = userClient.getByUUIDs(patientIdList)
                .stream()
                .collect(Collectors.toMap(PatientProfileOverview::getPatientId, item -> item));

        return TestAppointmentSlotPaginationResponse
                .builder()
                .currentPage(page)
                .totalElements(bookings.getTotalElements())
                .items(bookings.stream().map(it ->
                        TestAppointmentSlotItem.builder()
                                .bookingId(it.getBookingId())
                                .patientName(patientProfiles.get(it.getPatientId()).getPatientName())
                                .status(it.getTest().getStatus())
                                .testName(it.getTest().getTestName())
                                .reservedDate(it.getReservedDate())
                                .build()
                ).toList()).build();
    }
}
