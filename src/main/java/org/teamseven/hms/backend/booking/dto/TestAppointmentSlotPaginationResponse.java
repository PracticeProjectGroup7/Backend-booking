package org.teamseven.hms.backend.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestAppointmentSlotPaginationResponse {
    private long totalElements;
    private int currentPage;

    private List<TestAppointmentSlotItem> items;
}