package com.epam.agency.security;

import com.epam.finaltask.model.enums.Role;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityUser userDetails;

    private Authentication authentication;
    private String validToken;
    private final String invalidToken = "invalidToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Key testKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Key = Base64.getEncoder().encodeToString(testKey.getEncoded());

        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", base64Key);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_KEY", 3600L);

        userDetails = new SecurityUser(UUID.randomUUID().toString(), "User", "Password", Role.USER, false, null);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

        validToken = jwtService.generateToken(authentication);
    }

    @Test
    void shouldGenerateTokenTest() {
        String token = jwtService.generateToken(authentication);

        assertNotNull(token);
        assertTrue(token.startsWith("eyJ"));
    }

    @Test
    void shouldGetTokenFromCookiesTest() {
        Cookie cookie = new Cookie("TOKEN", validToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        String tokenFromCookie = jwtService.getTokenFromCookies(request);

        assertNotNull(tokenFromCookie);
        assertEquals(validToken, tokenFromCookie);
    }

    @Test
    void shouldReturnNullWhenNoTokenInCookiesTest() {
        when(request.getCookies()).thenReturn(null);
        String tokenFromCookie = jwtService.getTokenFromCookies(request);
        assertNull(tokenFromCookie);
    }

    @Test
    void shouldCheckIfTokenIsValidTest() {
        boolean isValid = jwtService.isTokenValid(validToken);
        assertTrue(isValid);

        boolean isInvalid = jwtService.isTokenValid(invalidToken);
        assertFalse(isInvalid);
    }

    @Test
    void shouldExtractAuthenticationFromValidTokenTest() {
        when(userDetailsService.loadUserByUsername("User")).thenReturn(userDetails);

        Authentication extractedAuthentication = jwtService.extractAuthentication(validToken);

        assertNotNull(extractedAuthentication);
        assertEquals(userDetails, extractedAuthentication.getPrincipal());
    }

    @Test
    void shouldExtractUsernameFromValidTokenTest() {
        String username = jwtService.extractUsername(validToken);

        assertEquals("User", username);
    }

    @Test
    void shouldThrowExceptionForInvalidTokenWhenExtractingClaimsTest() {
        assertThrows(JwtException.class, () -> jwtService.extractUsername(invalidToken));
    }
}
