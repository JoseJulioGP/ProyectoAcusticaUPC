package com.upc.acusticupc.auth.application.service;

import com.upc.acusticupc.auth.application.dto.AuthResponse;
import com.upc.acusticupc.auth.application.dto.LoginRequest;
import com.upc.acusticupc.auth.application.dto.RegisterRequest;
import com.upc.acusticupc.auth.application.dto.UserDTO;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    UserDTO register(RegisterRequest request);

    UserDTO getCurrentUser(String email);
}