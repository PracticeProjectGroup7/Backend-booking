package org.teamseven.hms.backend.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamseven.hms.backend.booking.annotation.PatientBookingAccessValidated;
import org.teamseven.hms.backend.booking.annotation.PatientDataRequestedMethod;
import org.teamseven.hms.backend.booking.dto.AddBookingRequest;
import org.teamseven.hms.backend.booking.service.BookingService;
import org.teamseven.hms.backend.booking.service.SlotCheckerService;
import org.teamseven.hms.backend.shared.ResponseWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services/")
public class BookingController {
    private static final String DATE_FORMAT  = "yyyy-MM-dd";
    @Autowired
    private BookingService bookingService;

    @Autowired
    private SlotCheckerService slotCheckerService;

    @PatientBookingAccessValidated
    @GetMapping(value = "booking-history/{patientId}")
    public ResponseEntity<ResponseWrapper> getBookingHistory(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ResponseEntity
                .ok()
                .body(
                        new ResponseWrapper.Success<>(
                                bookingService.getBookingHistory(patientId, page, pageSize)
                        )
                );
    }


    @PatientBookingAccessValidated(dataRequestMethod = PatientDataRequestedMethod.BOOKING_ID_PATH)
    @GetMapping(value = "booking-details/{bookingId}")
    public ResponseEntity<ResponseWrapper> getBookingDetails(
            @PathVariable UUID bookingId
    ) {
        return ResponseEntity
                .ok()
                .body(new ResponseWrapper.Success<>(bookingService.getBookingInfo(bookingId)));
    }

    @PostMapping(value = "booking")
    public ResponseEntity<ResponseWrapper> reserveBookingSlot(
            @RequestBody AddBookingRequest bookingRequest
    ) {
        return ResponseEntity
                .ok()
                .body(
                        new ResponseWrapper.Success<>(
                                bookingService.reserveSlot(bookingRequest)
                        )
                );
    }

    @GetMapping(value = "booking-schedules/{serviceId}")
    public ResponseEntity<ResponseWrapper> getAppointmentSlotsOnADay(
            @PathVariable UUID serviceId,
            @RequestParam String date
    ) {
        Date reservedDate = null;
        try {
            reservedDate = new SimpleDateFormat(DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date param passed in a bad format. use yyyy-MM-dd format");
        }

        return ResponseEntity.ok().body(
                new ResponseWrapper.Success<>(
                        slotCheckerService.getServiceSlots(serviceId, reservedDate)
                )
        );
    }
}
