package org.teamseven.hms.backend.sample.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.teamseven.hms.backend.shared.ResponseWrapper;

@RestController
@RequestMapping("/api/v1/samples")
public class SampleController {
    /**
     * Success response sample
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper> getSampleResponse() {
        return ResponseEntity.ok(
                new ResponseWrapper.Success<>("healthcheck")
        );
    }
}