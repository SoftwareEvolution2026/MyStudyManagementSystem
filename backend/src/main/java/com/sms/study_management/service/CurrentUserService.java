package com.sms.study_management.service;

import org.springframework.stereotype.Service;

import com.sms.study_management.exception.ResourceNotFoundException;
import com.sms.study_management.model.User;
import com.sms.study_management.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
