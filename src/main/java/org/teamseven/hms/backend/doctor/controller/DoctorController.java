package org.teamseven.hms.backend.doctor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.teamseven.hms.backend.doctor.service.DoctorService;
import org.teamseven.hms.backend.shared.ResponseWrapper;
import org.teamseven.hms.backend.shared.annotation.SampleAccessValidated;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {
    @Autowired DoctorService doctorService;

    @PostMapping
    public ResponseEntity<ResponseWrapper> getDoctorProfiles(@RequestBody List<UUID> uuids) {
        return ResponseEntity.ok().body(
                new ResponseWrapper.Success<>(doctorService.getDoctorProfiles(uuids))
        );
    }

}