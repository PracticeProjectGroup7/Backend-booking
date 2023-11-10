package org.teamseven.hms.backend.booking.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.teamseven.hms.backend.booking.dto.AppointmentBookingSlotItem;
import org.teamseven.hms.backend.booking.dto.AppointmentBookingSlotPaginationResponse;
import org.teamseven.hms.backend.booking.dto.UpdateAppointmentRequest;
import org.teamseven.hms.backend.booking.entity.AppointmentRepository;
import org.teamseven.hms.backend.booking.entity.Booking;
import org.teamseven.hms.backend.client.CatalogClient;
import org.teamseven.hms.backend.client.PatientProfileOverview;
import org.teamseven.hms.backend.client.ServiceOverview;
import org.teamseven.hms.backend.client.UserClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    @Autowired private AppointmentRepository repository;

    @Autowired private UserClient userClient;

    @Autowired private BookingService bookingService;

    @Autowired private CatalogClient catalogClient;

    public boolean updateAppointmentDetails(
            UUID appointmentId,
            UpdateAppointmentRequest appointmentRequest
    ) {
        return repository.setStatusAndCommentsForAppointment(
                appointmentRequest.getStatus(),
                appointmentRequest.getComments(),
                appointmentId
        ) == 1;
    }

    public AppointmentBookingSlotPaginationResponse getDoctorSlots(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        UUID doctorId = UUID.fromString(request.getAttribute("roleId").toString());
        ServiceOverview overview = catalogClient.getServiceOverviewByDoctorId(doctorId);
        int zeroBasedIdxPage = page - 1;

        Page<Booking> bookingPage = bookingService.findUpcomingServiceBookings(
                overview.getServiceId(),
                Pageable.ofSize(pageSize).withPage(zeroBasedIdxPage)
        );

        List<UUID> patientIdList = bookingPage.getContent().stream().map(Booking::getPatientId).toList();

        Map<UUID, PatientProfileOverview> patientProfiles = userClient.getByUUIDs(
                patientIdList
        ).stream().collect(Collectors.toMap(PatientProfileOverview::getPatientId, item -> item));

        return AppointmentBookingSlotPaginationResponse
                .builder()
                .currentPage(page)
                .totalElements(bookingPage.getTotalElements())
                .items(bookingPage.stream().map(it ->
                        AppointmentBookingSlotItem.builder()
                                .bookingId(it.getBookingId())
                                .patientName(patientProfiles.get(it.getPatientId()).getPatientName())
                                .status(it.getAppointment().getStatus())
                                .reservedDate(it.getReservedDate())
                                .build()
                ).toList()).build();
    }
}
