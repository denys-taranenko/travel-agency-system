package com.epam.agency.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthenticationRequest {

    @NotBlank(message = "{user.username.not.blank}")
    private String username;

    @NotBlank(message = "{user.password.not.blank}")
    private String password;
}
