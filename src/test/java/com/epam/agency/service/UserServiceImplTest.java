package com.epam.agency.service;

import com.epam.agency.dto.UserDTO;
import com.epam.agency.mapper.UserMapper;
import com.epam.agency.model.User;
import com.epam.agency.model.enums.Role;
import com.epam.agency.repository.UserRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    void registerUser_ShouldReturnUserDTO() {
        //Arrange
        UserDTO userDTO = UserDTO.builder()
                .username("TestUser123")
                .password("Passw0rd")
                .phoneNumber(null)
                .email("test@test.com")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .phoneNumber(null)
                .avatarPath(null)
                .role(Role.USER)
                .accountStatus(false)
                .balance(BigDecimal.valueOf(0))
                .orders(null)
                .avatarPath(null)
                .provider("TravelAgency")
                .build();

        String encryptedPassword = "$2a$12$Tq7niswv6pFvG/u/Xvic0Oae88eFXKsBsiB8IeLre8QSWB4/lNS32";

        when(userMapper.toUser(any(UserDTO.class))).thenReturn(user);
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(eq("Passw0rd"))).thenReturn(encryptedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(userDTO);

        //Act
        UserDTO registeredUserDTO = userService.registerUser(userDTO);

        //Assert
        assertThat(registeredUserDTO).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(eq("Passw0rd"));
    }

    @Test
    void updateUserAvatar_ShouldUpdateAvatarPath() {
        //Arrange
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String avatarPath = "bear.png";

        User user = User.builder()
                .id(UUID.fromString(userId))
                .avatarPath(null)
                .build();

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));

        //Act
        userService.updateUserAvatar(avatarPath, userId);

        //Assert
        assertThat(user.getAvatarPath()).isEqualTo(avatarPath);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeUserBalance_ShouldUpdateBalance() {
        //Arrange
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .balance(BigDecimal.ZERO)
                .build();

        BigDecimal amount = new BigDecimal(100);

        User user = User.builder()
                .id(UUID.fromString(userId))
                .balance(BigDecimal.ZERO)
                .build();

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));

        //Act
        userService.changeUserBalance(userDTO, amount);

        //Assert
        assertThat(user.getBalance()).isEqualTo(amount);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeUserStatus_ShouldToggleAccountStatus() {
        //Arrange
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        User user = User.builder()
                .id(UUID.fromString(userId))
                .accountStatus(true)
                .build();

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));

        //Act
        userService.changeUserStatus(userId);

        //Assert
        assertThat(user.isAccountStatus()).isFalse();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void getUserDTOById_ShouldReturnUserDTO() {
        //Arrange
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        User user = User.builder()
                .id(UUID.fromString(userId))
                .username("User")
                .build();

        UserDTO expectedUserDTO = UserDTO.builder()
                .id(userId)
                .username(user.getUsername())
                .build();

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(expectedUserDTO);

        //Act
        UserDTO result = userService.getUserDTOById(userId);

        //Assert
        assertThat(result.getUsername()).isEqualTo(expectedUserDTO.getUsername());
        assertThat(result.getId()).isEqualTo(expectedUserDTO.getId());
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUserDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(new User()));

        when(userRepository.findAllUsers(null, null, pageable)).thenReturn(userPage);

        // Act
        Page<UserDTO> result = userService.getAllUsers(null, null, pageable);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1)).findAllUsers(null, null, pageable);
    }
}
