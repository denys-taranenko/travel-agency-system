package com.epam.agency.security;

import com.epam.agency.model.User;
import com.epam.agency.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class SecurityUser implements UserDetails, OAuth2User {
    private final String id;
    private final String username;
    private final String password;
    private final Role role;
    private final boolean accountStatus;
    private Map<String, Object> attributes;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountStatus;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return username;
    }

    public static UserDetails fromUser(User user) {
        return new SecurityUser(
                user.getId().toString(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.isAccountStatus(),
                null);
    }

    public static SecurityUser fromOAuth2User(User user, OAuth2User oauth2User) {
        SecurityUser userDetails = (SecurityUser) fromUser(user);
        userDetails.setAttributes(oauth2User.getAttributes());
        return userDetails;
    }
}
