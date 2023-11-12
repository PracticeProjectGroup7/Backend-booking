package org.teamseven.hms.backend.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamseven.hms.backend.bill.dto.UpdateBillRequest;
import org.teamseven.hms.backend.bill.service.BillService;
import org.teamseven.hms.backend.booking.annotation.AdminEndpointAccessValidated;
import org.teamseven.hms.backend.booking.dto.ModifyBookingRequest;
import org.teamseven.hms.backend.booking.dto.ModifyTestRequest;
import org.teamseven.hms.backend.booking.service.BookingService;
import org.teamseven.hms.backend.shared.ResponseWrapper;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AdminBookingController {
    @Autowired
    private BillService billService;

    @Autowired
    private BookingService bookingService;

    @AdminEndpointAccessValidated
    @PatchMapping("/modify-booking")
    public ResponseEntity<ResponseWrapper> modifyBooking(
            @RequestBody ModifyBookingRequest modifyBookingRequest
    ) {
        return ResponseEntity.ok(new ResponseWrapper.Success<>(bookingService.modifyBooking(modifyBookingRequest)));
    }

    @AdminEndpointAccessValidated(isReceptionistAccessAllowed = true)
    @PatchMapping("/bills/{booking_id}")
    public ResponseEntity<ResponseWrapper> updateBillStatus(
            @PathVariable String booking_id,
            @RequestBody UpdateBillRequest updateBillRequest
    ) {
        return ResponseEntity.ok(new ResponseWrapper.Success<>(billService.updateBill(updateBillRequest, booking_id)));
    }

    @AdminEndpointAccessValidated
    @PatchMapping("/modify-test")
    public ResponseEntity<ResponseWrapper> modifyTest(
            @RequestBody ModifyTestRequest modifyTestRequest
    ) {
        return ResponseEntity.ok(new ResponseWrapper.Success<>(bookingService.modifyTest(modifyTestRequest)));
    }
}
