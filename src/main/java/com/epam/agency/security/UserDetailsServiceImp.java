package com.epam.agency.security;

import com.epam.agency.exception.NotFoundException;
import com.epam.agency.exception.status.StatusCodes;
import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return SecurityUser.fromUser(userRepository.findUserByUsername(username).orElseThrow(() ->
                new NotFoundException(StatusCodes.NOT_FOUND.name(), StatusMessages.USER_NOT_FOUND.getStatusMessage())));
    }
}
