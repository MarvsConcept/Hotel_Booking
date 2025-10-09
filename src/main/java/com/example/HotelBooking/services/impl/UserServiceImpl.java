package com.example.HotelBooking.services.impl;

import com.example.HotelBooking.dtos.LoginRequest;
import com.example.HotelBooking.dtos.RegistrationRequest;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.dtos.UserDTO;
import com.example.HotelBooking.entities.User;
import com.example.HotelBooking.enums.UserRole;
import com.example.HotelBooking.repositories.BookingRepository;
import com.example.HotelBooking.repositories.UserRepository;
import com.example.HotelBooking.security.JwtUtils;
import com.example.HotelBooking.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;

    @Override
    public Response registerUser(RegistrationRequest registrationRequest) {
        UserRole role = UserRole.CUSTOMER;
        if (registrationRequest.getRole() != null) {
            role = registrationRequest.getRole();
        }

        User userToSave = User.builder()
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .isActive(Boolean.TRUE)
                .build();

        userRepository.save(userToSave);

        return Response.builder()
                .status(200)
                .message("User Created Successfully!")
                .build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public Response getAllUsers() {
        return null;
    }

    @Override
    public Response getOwnAccountDetails() {
        return null;
    }

    @Override
    public Response getCurrentLoggedInUser() {
        return null;
    }

    @Override
    public Response updateOwnAccount(UserDTO userDTO) {
        return null;
    }

    @Override
    public Response deleteOwnAccount() {
        return null;
    }

    @Override
    public Response getMyBookingHistory() {
        return null;
    }
}
