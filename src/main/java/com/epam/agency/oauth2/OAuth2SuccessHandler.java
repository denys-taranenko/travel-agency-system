package com.epam.agency.oauth2;

import com.epam.agency.security.JwtService;
import com.epam.agency.security.SecurityUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof SecurityUser userDetails) {
            String token = jwtService.generateToken(authentication);

            Cookie cookie = new Cookie(JwtService.COOKIE, token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setDomain("localhost");
            cookie.setAttribute("SameSite", "None");
            cookie.setMaxAge(60 * 60 * 24 * 30);
            response.addCookie(cookie);

            getRedirectStrategy().sendRedirect(request, response, "/users/info");
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
