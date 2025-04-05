package com.epam.agency.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.AlreadyExistsException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.enums.Role;
import com.epam.finaltask.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_SuccessTest() {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("TestUser123");
        userDTO.setPassword("Passw0rd");
        userDTO.setRole("USER");
        userDTO.setAccountStatus(false);
        userDTO.setBalance(BigDecimal.valueOf(100.0));
        String encryptedPassword = "$2a$12$Tq7niswv6pFvG/u/Xvic0Oae88eFXKsBsiB8IeLre8QSWB4/lNS32";

        User user = User.builder()
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .role(Role.USER)
                .accountStatus(userDTO.isAccountStatus())
                .balance(userDTO.getBalance())
                .orders(null)
                .phoneNumber(null)
                .build();

        when(userMapper.toUser(any(UserDTO.class))).thenReturn(user);
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(eq("Passw0rd"))).thenReturn(encryptedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(userDTO);

        // When
        UserDTO registeredUserDTO = userService.registerUser(userDTO);

        // Then
        assertNotNull(registeredUserDTO, "Registered UserDTO should not be null");
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(eq("Passw0rd"));
    }

    @Test
    void checkUserData_UsernameIsAlreadyExistsInDB_ThrowEntityAlreadyExistsExceptionTest() {
        // Given
        UserDTO userDto = new UserDTO();
        userDto.setUsername("userName");
        userDto.setPassword("passwordTT19");
        String errorMessage = "status.user.already.exist";
        String errorCode = "CONFLICT";

        // When
        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(true);

        // Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () ->
                userService.registerUser(userDto));

        assertEquals(AlreadyExistsException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(errorCode, exception.getStatusCode());
    }

    @Test
    void changeUserBalance_SuccessTest() {
        // Given
        String userId = UUID.randomUUID().toString();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setBalance(BigDecimal.valueOf(1000));

        User user = new User();
        user.setId(UUID.fromString(userId));
        user.setBalance(BigDecimal.valueOf(1000));

        BigDecimal amountToAdd = BigDecimal.valueOf(500);

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        userService.changeUserBalance(userDTO, amountToAdd);

        // Then
        assertEquals(BigDecimal.valueOf(1500), user.getBalance(), "User balance should be updated correctly");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeUserBalance_NegativeAmountTest() {
        // Given
        String userId = UUID.randomUUID().toString();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);

        BigDecimal negativeAmount = BigDecimal.valueOf(-500);

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.changeUserBalance(userDTO, negativeAmount),
                "Expected NotFoundException to be thrown for negative amount");
    }

    @Test
    void changeUserStatus_SuccessTest() {
        // Given
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(UUID.fromString(userId));
        user.setAccountStatus(true);

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        userService.changeUserStatus(userId);

        // Then
        assertFalse(user.isAccountStatus(), "User status should be changed to inactive");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeUserStatus_UserNotFoundTest() {
        // Given
        String userId = UUID.randomUUID().toString();

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.changeUserStatus(userId),
                "Expected NotFoundException to be thrown when user is not found");
    }

    @Test
    void getUserDTOById_SuccessTest() {
        // Given
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(UUID.fromString(userId));
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setUsername("testuser");
        userDTO.setEmail("testuser@example.com");

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        // When
        UserDTO result = userService.getUserDTOById(userId);

        // Then
        assertNotNull(result, "UserDTO should not be null");
        assertEquals(userDTO.getId(), result.getId(), "User IDs should match");
        assertEquals(userDTO.getUsername(), result.getUsername(), "Usernames should match");

        verify(userRepository, times(1)).findById(UUID.fromString(userId));
        verify(userMapper, times(1)).toUserDTO(user);
    }

    @Test
    void getAllUsers_SuccessTest() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        UserDTO userDTO = new UserDTO();
        Page<User> usersPage = new PageImpl<>(Collections.singletonList(user), pageable, 1);

        when(userRepository.findAllUsers(null, null, pageable)).thenReturn(usersPage);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        // When
        Page<UserDTO> result = userService.getAllUsers(null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findAllUsers(null, null, pageable);
    }

    @Test
    void getUserByUsername_SuccessTest() {
        // Given
        String username = "testuser";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findUserByUsername(username);
    }

    @Test
    void saveUser_SuccessTest() {
        // Given
        User user = new User();
        user.setId(UUID.fromString(UUID.randomUUID().toString()));

        // When
        userService.saveUser(user);

        // Then
        verify(userRepository, times(1)).save(user);
    }
}
