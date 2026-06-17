package com.sms.study_management.service;


import com.sms.study_management.dto.LoginRequest;
import com.sms.study_management.dto.RegisterRequest;
import com.sms.study_management.exception.ResourceNotFoundException;
import com.sms.study_management.model.User;
import com.sms.study_management.repository.UserRepository;
import com.sms.study_management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();
        userRepository.save(user);
        return "User registered successfully";
    }

    public String login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        } catch (RuntimeException ex) {
            throw new BadCredentialsException("Invalid username or password", ex);
        }

        userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return tokenProvider.generateToken(req.getUsername());
    }
}
