package org.teamseven.hms.backend.booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@Entity
@SQLDelete(sql = "UPDATE bookings SET is_active = true WHERE id=?")
@Where(clause = "is_active = true")
@Table(name = "bookings")
public class Booking {
    @Id
    @Column(name="id", insertable=false)
    @GeneratedValue
    private UUID id;
    private UUID appointmentId;
    private UUID testId;

    @NotNull
    private UUID serviceId;

    @NotNull
    private UUID patientId;

    @Generated
    @Column(name="bill_number", insertable=false)
    private BigInteger billNumber;

    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    private BigDecimal amountPaid;

    private OffsetDateTime paidAt;

    @NotNull
    private Long gst = 8L;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private OffsetDateTime endTime;

    @NotNull
    private boolean isActive = true;

    @Generated()
    @Column(name="created_at", insertable=false)
    private OffsetDateTime createdAt;

    private OffsetDateTime modifiedAt;
}