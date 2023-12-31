package org.teamseven.hms.backend.booking.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE bookings SET is_active = false WHERE booking_id=?")
@Where(clause = "is_active = true")
@Table(name = "bookings")
public class Booking {
    @Id
    @Column(name="booking_id", insertable=false)
    @GeneratedValue
    private UUID bookingId;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "test_id")
    private Test test;

    private UUID serviceId;

    @NotNull
    private UUID patientId;

    @Generated
    @Column(name="bill_number", insertable=false)
    private BigInteger billNumber;

    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    private BigDecimal amountPaid;

    private OffsetDateTime paidAt = OffsetDateTime.now();

    @NotNull
    private Long gst = 8L;

    @NotNull
    private String slots;

    @NotNull
    private String reservedDate;

    @NotNull
    private boolean isActive = true;

    @Generated
    @Column(name="created_at", insertable=false)
    private OffsetDateTime createdAt;

    private OffsetDateTime modifiedAt;

    @OneToMany
    @JoinColumn(name = "booking_id")
    private List<Fee> fee;
}