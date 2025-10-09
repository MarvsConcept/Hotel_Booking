package com.example.HotelBooking.services;

import com.example.HotelBooking.dtos.LoginRequest;
import com.example.HotelBooking.dtos.RegistrationRequest;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.dtos.UserDTO;

public interface UserService {

    Response registerUser(RegistrationRequest registrationRequest);

    Response loginUser(LoginRequest loginRequest);

    Response getAllUsers();

    Response getOwnAccountDetails();

    Response getCurrentLoggedInUser();

    Response updateOwnAccount(UserDTO userDTO);

    Response deleteOwnAccount();

    Response getMyBookingHistory();

}
