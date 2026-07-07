package com.sms.study_management.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sms.study_management.exception.ResourceNotFoundException;
import com.sms.study_management.model.User;
import com.sms.study_management.repository.UserRepository;

class CurrentUserServiceTest {

    @Test
    void shouldResolveExistingUserByUsername() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .passwordHash("hash")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        CurrentUserService currentUserService = new CurrentUserService(userRepository);

        User resolvedUser = currentUserService.requireUser("alice");

        assertEquals("alice", resolvedUser.getUsername());
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        CurrentUserService currentUserService = new CurrentUserService(userRepository);

        assertThrows(ResourceNotFoundException.class, () -> currentUserService.requireUser("missing"));
    }
}
