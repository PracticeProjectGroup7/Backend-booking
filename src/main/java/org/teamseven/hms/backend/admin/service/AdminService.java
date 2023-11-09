package org.teamseven.hms.backend.admin.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestParam;
import org.teamseven.hms.backend.admin.dto.*;
import org.teamseven.hms.backend.booking.entity.Booking;
import org.teamseven.hms.backend.booking.entity.BookingRepository;
import org.teamseven.hms.backend.client.CatalogClient;
import org.teamseven.hms.backend.client.CreateDoctorService;
import org.teamseven.hms.backend.user.Role;
import org.teamseven.hms.backend.user.User;
import org.teamseven.hms.backend.user.UserRepository;
import org.teamseven.hms.backend.user.UserRequest;
import org.teamseven.hms.backend.user.dto.CreateHospitalAccountRequest;
import org.teamseven.hms.backend.user.entity.*;
import org.teamseven.hms.backend.user.service.UserService;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class AdminService  {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CatalogClient catalogClient;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private UserService userService;


    @Transactional
    public boolean modifyBooking(ModifyBookingRequest modifyBookingRequest) {
        Optional<Booking> existingBooking = bookingRepository
                .findByServiceIdAndReservedDateAndSlot(
                        modifyBookingRequest.getServiceId(),
                        modifyBookingRequest.getNewReservedDate(),
                        modifyBookingRequest.getNewSlot()
                );
        if(existingBooking.isPresent()) {
            throw new IllegalStateException("Unable to modify booking, booking already exists!");
        }

        return bookingRepository
                .updateBooking(
                        modifyBookingRequest.getPatientId(),
                        modifyBookingRequest.getServiceId(),
                        modifyBookingRequest.getOldReservedDate(),
                        modifyBookingRequest.getOldSlot(),
                        modifyBookingRequest.getNewReservedDate(),
                        modifyBookingRequest.getNewSlot()
                ) == 1;
    }

    @Transactional
    public boolean modifyTest(ModifyTestRequest modifyTestRequest) {
        Optional<Booking> existingTest = bookingRepository
                .checkTestExists(
                        modifyTestRequest.getNewReservedDate(),
                        modifyTestRequest.getServiceId()
                );
        if(existingTest.isPresent()) {
            throw new IllegalStateException("Unable to modify test, test already exists!");
        }

        return bookingRepository
                .updateTest(
                        modifyTestRequest.getPatientId(),
                        modifyTestRequest.getServiceId(),
                        modifyTestRequest.getOldReservedDate(),
                        modifyTestRequest.getNewReservedDate()
                ) == 1;
    }

    public UUID createStaffAccount(
            CreateHospitalAccountRequest request
    ) {
        try {
            User userExists = userRepository.findByEmail(request.getEmail());
            if (userExists != null) {
                throw new IllegalArgumentException("User " + request.getEmail() + " already exists!");
            }
            return transactionTemplate.execute(status -> {
                User user = User.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .gender(request.getGender())
                        .address(request.getAddress())
                        .phone(request.getPhoneNumber())
                        .nric(request.getNric())
                        .dateOfBirth(request.getDateOfBirth())
                        .type(request.getRole().name())
                        .role(request.getRole())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .build();

                User createdUser = userRepository.save(user);

                if (request.getRole() == Role.DOCTOR) {
                    Doctor doctor = createDoctorAccount(createdUser, request);
                    Doctor createdDoctor = doctorRepository.save(doctor);
                    catalogClient.createNewService(
                            CreateDoctorService.builder()
                                    .doctorId(createdDoctor.getDoctorId())
                                    .name(createdUser.getName())
                                    .description(createdDoctor.getSpeciality())
                                    .estimatedPrice(createdDoctor.getConsultationFees())
                                    .build()
                    );
                }

                if (request.getRole() == Role.STAFF || request.getRole() == Role.LAB_SUPPORT_STAFF) {
                    Staff staff = createStaffAccount(createdUser, request.getRole());
                    staffRepository.save(staff);
                }

                return createdUser.getUserId();
            });
        } catch (Exception e) {
            Logger.getAnonymousLogger().severe("Exception when creating account " + e);
            throw e;
        }
    }

    private Doctor createDoctorAccount(
            User createdUser,
            CreateHospitalAccountRequest request
    ) {
        return Doctor.builder()
                .user(createdUser)
                .speciality(request.getSpecialty())
                .consultationFees(request.getConsultationFees())
                .yearsOfExperience(request.getYearsOfExperience())
                .build();
    }

    private Staff createStaffAccount(
            User createdUser,
            Role role
    ) {
        return Staff.builder()
                .user(createdUser)
                .type(role.name())
                .isActive(1)
                .build();
    }

    public RetrievePatientsPaginationResponse getAllPatients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        int zeroBasedIndexPage = page - 1;
        Page<Patient> patientList = patientRepository.getPatients(Pageable.ofSize(pageSize).withPage(zeroBasedIndexPage));
        return RetrievePatientsPaginationResponse.builder()
                .currentPage(page)
                .totalElements(patientList.getTotalElements())
                .items(patientList.stream().map(it ->
                                RetrievePatientItem.builder()
                                        .userId(it.getUser().getUserId())
                                        .patientId(it.getPatientId())
                                        .firstname(it.getUser().getFirstName())
                                        .lastName(it.getUser().getLastName())
                                        .email(it.getUser().getEmail())
                                        .build()
                        ).toList())
                .build();
    }

    public Page<User> getAllStaff(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        int zeroBasedIndexPage = page - 1;
        return userRepository.getAllStaffAccounts(Pageable.ofSize(pageSize).withPage(zeroBasedIndexPage));
    }

    public Staff getStaffProfile(UUID staffId) {
        return staffRepository.findById(staffId).orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public User updateStaffProfile(UserRequest userRequest, UUID user_id) {
        User user = userRepository.findById(user_id).orElseThrow(NoSuchElementException::new);
        user = userService.setUpdateFields(user, userRequest);
        return userRepository.save(user);
    }
}
