package com.epam.agency.auth;

import com.epam.finaltask.exception.AccessDeniedException;
import com.epam.finaltask.exception.InvalidDataException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    private AuthenticationRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authRequest = new AuthenticationRequest();
        authRequest.setUsername("user");
        authRequest.setPassword("password");

        user = new User();
        user.setUsername("user");
        user.setPassword("encodedPassword");
        user.setAccountStatus(false);
    }

    @Test
    void shouldAuthenticateSuccessfullyTest() {
        when(userRepository.findUserByUsername(authRequest.getUsername())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn("validJwtToken");

        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("validJwtToken", response.getAccessToken());
        verify(userRepository).findUserByUsername(authRequest.getUsername());
        verify(passwordEncoder).matches(authRequest.getPassword(), user.getPassword());
        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(authentication);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFoundTest() {
        when(userRepository.findUserByUsername(authRequest.getUsername())).thenReturn(java.util.Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> authenticationService.authenticate(authRequest));
        assertEquals("status.user.not.found", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidDataExceptionWhenPasswordIsWrongTest() {
        when(userRepository.findUserByUsername(authRequest.getUsername())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(false);

        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> authenticationService.authenticate(authRequest));
        assertEquals("status.wrong.password", exception.getMessage());
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenAccountIsBlockedTest() {
        user.setAccountStatus(true);
        when(userRepository.findUserByUsername(authRequest.getUsername())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> authenticationService.authenticate(authRequest));
        assertEquals("status.forbidden.blocked.user", exception.getMessage());
    }
}
