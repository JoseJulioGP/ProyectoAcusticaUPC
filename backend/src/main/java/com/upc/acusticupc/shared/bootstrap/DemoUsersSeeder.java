package com.upc.acusticupc.shared.bootstrap;

import com.upc.acusticupc.auth.domain.model.Role;
import com.upc.acusticupc.auth.domain.model.User;
import com.upc.acusticupc.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)   
@RequiredArgsConstructor
public class DemoUsersSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUsersSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${acusticupc.bootstrap.analyst-password:AnalystUpc2026!}")
    private String analystPassword;

    @Value("${acusticupc.bootstrap.viewer-password:ViewerUpc2026!}")
    private String viewerPassword;

    @Override
    public void run(String... args) {
        seed("analyst@upc.edu.co", "Analista AcústicaUPC", Role.ANALYST, analystPassword);
        seed("viewer@upc.edu.co", "Observador AcústicaUPC", Role.VIEWER, viewerPassword);
    }

    private void seed(String email, String fullName, Role role, String rawPassword) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.info(">>> Usuario demo ya existe ({}), no se reinserta.", email);
            return;
        }
        User u = User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .active(true)
                .build();
        userRepository.save(u);
        log.info(">>> Usuario demo sembrado: {} (rol {})", email, role);
    }
}
