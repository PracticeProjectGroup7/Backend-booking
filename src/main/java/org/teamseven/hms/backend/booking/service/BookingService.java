package org.teamseven.hms.backend.booking.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.teamseven.hms.backend.booking.dto.*;
import org.teamseven.hms.backend.booking.entity.*;
import org.teamseven.hms.backend.client.CatalogClient;
import org.teamseven.hms.backend.client.ServiceOverview;
import org.teamseven.hms.backend.user.dto.PatientProfileOverview;
import org.teamseven.hms.backend.user.entity.Patient;
import org.teamseven.hms.backend.user.entity.PatientRepository;
import org.teamseven.hms.backend.user.service.PatientService;

import java.io.IOException;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class BookingService {

    private static final String SLOT_DELIMITER = ",";
    private static final String APPOINTMENT_DESCRIPTION_PREFIX = "Appointment with Dr. ";

    @Autowired private BookingRepository bookingRepository;

    // TODO sheila: decouple booking service from patient / user DAO in favour of convenience when
    // migrating to microservices. make sure that @service(s) either only depend on other controllers
    // or other services of different domains.
    @Autowired private PatientRepository patientRepository;

    @Autowired private CatalogClient catalogClient;

    @Autowired private PatientService patientService;

    @Autowired private AppointmentRepository appointmentRepository;

    @Autowired private TestRepository testRepository;

    @Autowired private FeeRepository feeRepository;

    public BookingPaginationResponse getBookingHistory(
            UUID patientId,
            int page,
            int pageSize
    ) {
        String patientName = getPatientName(patientId);

        int zeroBasedIndexPage = page - 1;
        Page<Booking> bookingPage = bookingRepository.findPatientBookingsWithPagination(
                patientId.toString(),
                Pageable.ofSize(pageSize).withPage(zeroBasedIndexPage)
        );

        List<BookingOverview> bookingList = bookingPage.map(getBookingOverview).toList();

        return BookingPaginationResponse
                .builder()
                .bookingList(bookingList)
                .totalElements(bookingPage.getTotalElements())
                .patientName(patientName)
                .build();
    }

    private String getPatientName(UUID patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            throw new NoSuchElementException("Something went wrong when fetching patient records");
        }

        return patient.get().getUser().getName();
    }

    private final Function<Booking, String[]> getSlots = it ->
            it.getSlots().split(SLOT_DELIMITER);

    private final BiFunction<BookingType, String, String> getDescription = (
            bookingType,
            serviceName
    ) -> switch (bookingType) {
        case TEST -> serviceName;
        case APPOINTMENT -> APPOINTMENT_DESCRIPTION_PREFIX + serviceName;
    };


    private final Function<Booking, BookingOverview> getBookingOverview = it -> {
        ServiceOverview serviceOverview = catalogClient.getServiceOverview(it.getServiceId());

        return BookingOverview
                .builder()
                .bookingDate(it.getReservedDate())
                .slots(getSlots.apply(it))
                .bookingId(it.getBookingId())
                .type(BookingType.valueOf(serviceOverview.getType()))
                .bookingDescription(
                        getDescription.apply(BookingType.valueOf(
                                        serviceOverview.getType()),
                                serviceOverview.getName())
                )
                .build();
    };

    public Booking getBookingById(UUID id) {
        return bookingRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public BookingInfoResponse getBookingInfo(UUID id) {
        Booking booking = getBookingById(id);

        ServiceOverview serviceOverview = catalogClient.getServiceOverview(booking.getServiceId());

        PatientProfileOverview patientProfileOverview = patientService.getPatientProfile(booking.getPatientId());

        PatientDataBookingDetails patientDetails = PatientDataBookingDetails.builder()
                .dateOfBirth(patientProfileOverview.getDateOfBirth())
                .patientName(patientProfileOverview.getPatientName())
                .gender(patientProfileOverview.getGender())
                .build();

        return BookingInfoResponse
                .builder()
                .bookingType(BookingType.valueOf(serviceOverview.getType()))
                .bookingDate(booking.getReservedDate())
                .slots(getSlots.apply(booking))
                .details(getBookingDetails.apply(booking, serviceOverview))
                .patientDetails(patientDetails)
                .build();
    }

    private final BiFunction<Booking, ServiceOverview, BookingDetails> getBookingDetails = (booking, serviceOverview) -> {
        return switch (BookingType.valueOf(serviceOverview.getType())) {
            case TEST -> {
                Test test = booking.getTest();
                yield BookingDetails.Test.builder()
                        .testName(serviceOverview.getName())
                        .testResult(test.getTestReport())
                        .testStatus(test.getStatus().toString())
                        .testId(test.getTestId().toString())
                        .build();
            }
            case APPOINTMENT -> {
                Appointment appointment = booking.getAppointment();
                yield BookingDetails.Appointment.builder()
                        .doctorName(serviceOverview.getName())
                        .department(serviceOverview.getDescription())
                        .comments(appointment.getDiagnosis())
                        .appointmentStatus(appointment.getStatus().toString())
                        .appointmentId(appointment.getAppointmentId().toString())
                        .build();
            }
        };
    };



    @Transactional
    public Booking reserveSlot(AddBookingRequest bookingRequest) {
        ServiceOverview serviceOverview = catalogClient.getServiceOverview(bookingRequest.getServiceId());
        Booking booked = new Booking();
        if (Objects.equals(bookingRequest.getType(), BookingType.APPOINTMENT)) {
            if(bookingExists(bookingRequest.getAppointmentDate(), bookingRequest.getSelectedSlot(), bookingRequest.getServiceId())) {
                throw new IllegalStateException("Appointment already exists!");
            }
            var appointment = Appointment.builder()
                    .patientId(bookingRequest.getPatientId())
                    .isActive(true)
                    .build();
            appointmentRepository.save(appointment);
            booked = book(bookingRequest, appointment.getAppointmentId(), bookingRequest.getServiceId());
        } else if (Objects.equals(bookingRequest.getType(), BookingType.TEST)) {
            if(testExists(bookingRequest.getAppointmentDate(), bookingRequest.getServiceId())) {
                throw new IllegalStateException("Test already exists!");
            }
            var test = Test.builder()
                    .patientId(bookingRequest.getPatientId())
                    .testDate(bookingRequest.getAppointmentDate())
                    .testName(serviceOverview.getName())
                    .status(TestStatus.PENDING)
                    .isActive(true)
                    .build();
            testRepository.save(test);
            booked = book(bookingRequest, test.getTestId(), bookingRequest.getServiceId());
        }
        return booked;
    }

    private boolean testExists(String date, UUID serviceId) {
        Optional<Booking> booking = bookingRepository.checkTestExists(date, String.valueOf(serviceId));
        return booking.isPresent();
    }

    private Booking book(AddBookingRequest bookingRequest, UUID id, UUID serviceId) {
        Appointment appointment = new Appointment();
        Test test = new Test();
        if(Objects.equals(bookingRequest.getType(), BookingType.APPOINTMENT)) {
            appointment.setAppointmentId(id);
        } else {
            test.setTestId(id);
        }
        var booking = Booking.builder()
                .patientId(bookingRequest.getPatientId())
                .appointment(Objects.equals(bookingRequest.getType(), BookingType.APPOINTMENT) ? appointment : null)
                .test(Objects.equals(bookingRequest.getType(), BookingType.TEST) ? test : null )
                .serviceId(serviceId)
                .billStatus(BillStatus.UNPAID)
                .slots(bookingRequest.getSelectedSlot())
                .gst(8L)
                .reservedDate(bookingRequest.getAppointmentDate())
                .isActive(true)
                .build();
        var savedBooking =  bookingRepository.save(booking);
        var platformFee = Fee.builder()
                .booking_id(savedBooking.getBookingId())
                .type(FeeType.PLATFORM)
                .price(BigDecimal.valueOf(10))
                .isActive(true)
                .build();
        var serviceFee = Fee.builder()
                .booking_id(savedBooking.getBookingId())
                .type(FeeType.SERVICE)
                .price(BigDecimal.valueOf(5))
                .isActive(true)
                .build();
        feeRepository.save(platformFee);
        feeRepository.save(serviceFee);
        return savedBooking;
    }

    private boolean bookingExists(String date, String slot, UUID serviceId) {
        Optional<Booking> booking = bookingRepository.findByAppointmentDate(date, slot, String.valueOf(serviceId));
        return booking.isPresent();
    }

    public Page<Booking> findUpcomingTestBookings(Pageable pageable) {
        return bookingRepository.findUpcomingTestBookings(
                LocalDate.now().toString(),
                pageable
        );
    }

    public Page<Booking> findUpcomingServiceBookings(UUID serviceId, Pageable pageable) {
        return bookingRepository.findUpcomingServiceBookings(
                serviceId.toString(),
                LocalDate.now().toString(),
                pageable
        );
    }
}
