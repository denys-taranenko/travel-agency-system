package com.epam.agency.oauth2;

import com.epam.agency.model.User;
import com.epam.agency.model.enums.Role;
import com.epam.agency.repository.UserRepository;
import com.epam.agency.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String providerId = userRequest.getClientRegistration().getRegistrationId();

        return switch (providerId.toLowerCase()) {
            case "github" -> processGitHubUser(oAuth2User, userRequest);
            case "google" -> processGoogleUser(oAuth2User, userRequest);
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + providerId);
        };
    }

    private SecurityUser processGitHubUser(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String login = (String) attributes.get("login");
        String email = getGitHubEmail(oAuth2User, userRequest.getAccessToken(), login);

        User user = userRepository.findUserByEmail(email)
                .orElseGet(() -> createNewUser(email, login, userRequest));

        return SecurityUser.fromOAuth2User(user, oAuth2User);
    }

    private SecurityUser processGoogleUser(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("name");

        User user = userRepository.findUserByEmail(email)
                .orElseGet(() -> createNewUser(email, username, userRequest));

        return SecurityUser.fromOAuth2User(user, oAuth2User);
    }

    private User createNewUser(String email, String username, OAuth2UserRequest userRequest) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username.replace(" ", "-"));
        newUser.setRole(Role.USER);
        newUser.setBalance(BigDecimal.ZERO);
        newUser.setProvider(userRequest.getClientRegistration().getClientName());
        return userRepository.save(newUser);
    }

    private String getGitHubEmail(OAuth2User oAuth2User, OAuth2AccessToken accessToken, String login) {
        String email = oAuth2User.getAttribute("email");
        if (email != null) return email;

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + accessToken.getTokenValue());

            ResponseEntity<Map[]> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map[].class);

            return Arrays.stream(Objects.requireNonNull(response.getBody()))
                    .filter(primary -> Boolean.TRUE.equals(primary.get("primary")))
                    .findFirst()
                    .map(primary -> (String) primary.get("email"))
                    .orElseThrow(() -> new OAuth2AuthenticationException("GitHub email not found"));
        } catch (Exception exception) {
            return login + "@users.noreply.github.com";
        }
    }
}
