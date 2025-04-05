package com.epam.agency.service;

import com.epam.agency.dto.UserDTO;
import com.epam.agency.exception.AlreadyExistsException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.exception.NotFoundException;
import com.epam.agency.exception.status.StatusCodes;
import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.mapper.UserMapper;
import com.epam.agency.model.User;
import com.epam.agency.model.enums.Role;
import com.epam.agency.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.internal.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        validateAlreadyExistsException(userDTO);
        User user = userMapper.toUser(userDTO);

        user.setPhoneNumber(user.getPhoneNumber());
        user.setEmail(user.getEmail());
        user.setBalance(BigDecimal.ZERO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setAvatarPath(null);
        user.setProvider("TravelAgency");

        User savedUser = userRepository.save(user);
        return userMapper.toUserDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        UserDTO currentUser = getUserDTO();
        User user = userMapper.toUser(currentUser);

        if (userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().trim().isEmpty()) {
            if (userRepository.existsByPhoneNumberAndIdNot(userDTO.getPhoneNumber(), UUID.fromString(currentUser.getId()))) {
                throw new AlreadyExistsException(StatusCodes.CONFLICT.name(), StatusMessages.PHONE_NUMBER_ALREADY_EXISTS.getStatusMessage());
            }
            user.setPhoneNumber(userDTO.getPhoneNumber());
        } else {
            user.setPhoneNumber(null);
        }

        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmailAndIdNot(userDTO.getEmail(), UUID.fromString(currentUser.getId()))) {
                throw new AlreadyExistsException(StatusCodes.CONFLICT.name(), StatusMessages.EMAIL_ALREADY_EXISTS.getStatusMessage());
            }
            user.setEmail(userDTO.getEmail());
        } else {
            user.setEmail(currentUser.getEmail());
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserDTO(savedUser);
    }

    @Override
    public void updateUserAvatar(String avatar, String userId) {
        User user = getUserById(userId);
        user.setAvatarPath(avatar);
        userRepository.save(user);
    }

    @Override
    public void changeUserBalance(UserDTO userDTO, BigDecimal amount) {
        User user = getUserById(userDTO.getId());

        if (amount.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(new BigDecimal(20000)) > 0) {
            throw new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.BAD_AMOUNT.getStatusMessage());
        }

        BigDecimal currentBalance = user.getBalance();
        currentBalance = currentBalance.add(amount);
        user.setBalance(currentBalance);

        userMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    public void changeUserStatus(String userId) {
        User user = getUserById(userId);
        user.setAccountStatus(!user.isAccountStatus());
        userRepository.save(user);
    }

    @Override
    public UserDTO getUserDTO() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = getUserByUsername(username);
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDTO getUserDTOById(String userId) {
        User user = getUserById(userId);
        return userMapper.toUserDTO(user);
    }

    @Override
    public Page<UserDTO> getAllUsers(Boolean accountStatus, String search, Pageable pageable) {
        Page<User> allUsers = userRepository.findAllUsers(accountStatus, search, pageable);
        return allUsers.map(userMapper::toUserDTO);
    }

    @Override
    public User getUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId)).orElseThrow(() ->
                new NotFoundException(StatusCodes.NOT_FOUND.name(), StatusMessages.USER_NOT_FOUND.getStatusMessage()));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username).orElseThrow(() ->
                new NotFoundException(StatusCodes.NOT_FOUND.name(), StatusMessages.USER_NOT_FOUND.getStatusMessage()));
    }

    @Override
    public UserDTO getUserDTOByEmail(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow(() ->
                new NotFoundException(StatusCodes.NOT_FOUND.name(), StatusMessages.USER_NOT_FOUND.getStatusMessage()));
        return userMapper.toUserDTO(user);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    private void validateAlreadyExistsException(UserDTO userDTO) {
        validateField(userDTO.getUsername(), userRepository::existsByUsername, StatusMessages.USERNAME_ALREADY_EXISTS);
        validateField(userDTO.getPhoneNumber(), userRepository::existsByPhoneNumber, StatusMessages.PHONE_NUMBER_ALREADY_EXISTS);
        validateField(userDTO.getEmail(), userRepository::existsByEmail, StatusMessages.EMAIL_ALREADY_EXISTS);
    }

    private void validateField(String value, Function<String, Boolean> existsMethod, StatusMessages statusMessage) {
        if (existsMethod.apply(value)) {
            throw new AlreadyExistsException(StatusCodes.CONFLICT.name(), statusMessage.getStatusMessage());
        }
    }
}
