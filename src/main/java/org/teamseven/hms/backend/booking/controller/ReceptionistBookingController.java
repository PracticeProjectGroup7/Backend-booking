package org.teamseven.hms.backend.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamseven.hms.backend.bill.dto.UpdateBillRequest;
import org.teamseven.hms.backend.bill.service.BillService;
import org.teamseven.hms.backend.booking.dto.ModifyBookingRequest;
import org.teamseven.hms.backend.booking.dto.ModifyTestRequest;
import org.teamseven.hms.backend.booking.service.BookingService;
import org.teamseven.hms.backend.shared.ResponseWrapper;

@RestController
@RequestMapping("/api/v1/recep")
@RequiredArgsConstructor
public class ReceptionistBookingController {
    @Autowired
    private BillService billService;

    @Autowired
    private BookingService bookingService;

    @PatchMapping("/modify-booking")
    public ResponseEntity<ResponseWrapper> modifyBooking(
            @RequestBody ModifyBookingRequest modifyBookingRequest
    ) {
        return ResponseEntity.ok(new ResponseWrapper.Success<>(bookingService.modifyBooking(modifyBookingRequest)));
    }

    @GetMapping("/bills")
    public ResponseEntity<ResponseWrapper> getBillByBookingId(
            @PathVariable String booking_id
    ) {
        return ResponseEntity.ok(new ResponseWrapper.Success<>(billService.getBillByBookingId(booking_id)));
    }

    @PatchMapping("/bills/{booking_id}")
    public ResponseEntity<ResponseWrapper> updateBillStatus(
            @PathVariable String booking_id,
            @RequestBody UpdateBillRequest updateBillRequest
    ) {
        return ResponseEntity.ok(new ResponseWrapper.Success<>(billService.updateBill(updateBillRequest, booking_id)));
    }

    @PatchMapping("/modify-test")
    public ResponseEntity<ResponseWrapper> modifyTest(
            @RequestBody ModifyTestRequest modifyTestRequest
    ) {
        return ResponseEntity.ok(new ResponseWrapper.Success<>(bookingService.modifyTest(modifyTestRequest)));
    }
}
