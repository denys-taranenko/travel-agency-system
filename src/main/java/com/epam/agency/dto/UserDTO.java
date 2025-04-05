package com.epam.agency.dto;

import com.epam.agency.annotation.ValidPhone;
import com.epam.agency.dto.group.OnChange;
import com.epam.agency.dto.group.OnCreate;
import com.epam.agency.dto.group.OnUpdate;
import com.epam.agency.util.RegexConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String id;

    @Pattern(groups = {OnCreate.class, OnUpdate.class}, regexp = RegexConstant.USERNAME_REGEX, message = "{user.username.pattern}")
    private String username;

    @Pattern(groups = OnCreate.class, regexp = RegexConstant.PASSWORD_REGEX, message = "{user.password.pattern}")
    private String password;

    private String role;
    private List<OrderDTO> orders;

    @ValidPhone(groups = {OnCreate.class, OnUpdate.class})
    private String phoneNumber;

    private BigDecimal balance;

    @NotNull(groups = OnChange.class)
    private boolean accountStatus;

    private String avatarPath;

    @Email(groups = {OnCreate.class, OnUpdate.class}, message = "{user.email.pattern}")
    @NotBlank(groups = OnCreate.class, message = "{user.email.not.null}")
    private String email;

    private String provider;
}
