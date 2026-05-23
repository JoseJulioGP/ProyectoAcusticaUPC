package com.upc.acusticupc.auth.application.service;

import com.upc.acusticupc.auth.application.dto.AuthResponse;
import com.upc.acusticupc.auth.application.dto.LoginRequest;
import com.upc.acusticupc.auth.application.dto.RegisterRequest;
import com.upc.acusticupc.auth.application.dto.UserDTO;
import com.upc.acusticupc.auth.application.mapper.UserMapper;
import com.upc.acusticupc.auth.domain.model.Role;
import com.upc.acusticupc.auth.domain.model.User;
import com.upc.acusticupc.auth.domain.repository.UserRepository;
import com.upc.acusticupc.auth.infrastructure.security.JwtService;
import com.upc.acusticupc.shared.exception.DomainException;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new DomainException("Credenciales inválidas");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new DomainException("Credenciales inválidas"));

        if (!user.canOperate()) {
            throw new DomainException("La cuenta está inactiva");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of("role", user.getRole().name(), "uid", user.getId().toString())
        );

        return AuthResponse.of(token, jwtService.getExpirationMs(), UserMapper.toDto(user));
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DomainException("Ya existe un usuario con ese email");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role() != null ? request.role() : Role.VIEWER)
                .active(true)
                .build();

        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return UserMapper.toDto(user);
    }
}
