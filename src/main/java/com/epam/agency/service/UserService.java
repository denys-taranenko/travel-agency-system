package com.epam.agency.service;

import com.epam.agency.dto.UserDTO;
import com.epam.agency.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface UserService {
    UserDTO registerUser(UserDTO userDTO);

    UserDTO updateUser(UserDTO userDTO);

    void updateUserAvatar(String avatar, String userId);

    void changeUserBalance(UserDTO userDTO, BigDecimal amount);

    void changeUserStatus(String userId);

    UserDTO getUserDTO();

    UserDTO getUserDTOById(String userId);

    Page<UserDTO> getAllUsers(Boolean accountStatus, String search, Pageable pageable);

    User getUserById(String userId);

    User getUserByUsername(String username);

    UserDTO getUserDTOByEmail(String email);

    void saveUser(User user);
}
