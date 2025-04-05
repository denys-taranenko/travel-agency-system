package com.epam.agency.auth;

import com.epam.agency.exception.AccessDeniedException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.exception.NotFoundException;
import com.epam.agency.exception.status.StatusCodes;
import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.model.User;
import com.epam.agency.repository.UserRepository;
import com.epam.agency.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        validateData(authenticationRequest);

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUsername(),
                authenticationRequest.getPassword())
        );

        String accessToken = jwtService.generateToken(authentication);
        return new AuthenticationResponse(accessToken);
    }

    private void validateData(AuthenticationRequest authenticationRequest) {
        User user = userRepository.findUserByUsername(authenticationRequest.getUsername()).orElseThrow(() ->
                new NotFoundException(StatusCodes.NOT_FOUND.name(), StatusMessages.USER_NOT_FOUND.getStatusMessage()));

        if (!(passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword()))) {
            throw new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.WRONG_PASSWORD.getStatusMessage());
        }

        if (user.isAccountStatus()) {
            throw new AccessDeniedException(StatusCodes.FORBIDDEN.name(), StatusMessages.ACCOUNT_BLOCKED.getStatusMessage());
        }
    }
}
