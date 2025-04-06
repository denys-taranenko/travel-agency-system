package com.epam.agency.controller;

import com.epam.agency.auth.AuthenticationRequest;
import com.epam.agency.auth.AuthenticationResponse;
import com.epam.agency.auth.AuthenticationService;
import com.epam.agency.dto.UserDTO;
import com.epam.agency.exception.*;
import com.epam.agency.exception.status.StatusCodes;
import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.security.PasswordResetService;
import com.epam.agency.service.UserService;
import com.epam.agency.util.MessageUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageUtil messageUtil;

    @Autowired
    private AuthenticationController authenticationController;

    private HttpServletResponse httpServletResponse;
    private RedirectAttributes redirectAttributes;
    private BindingResult bindingResult;
    private ModelMap modelMap;

    @BeforeEach
    void setUp() {
        httpServletResponse = mock(HttpServletResponse.class);
        redirectAttributes = mock(RedirectAttributes.class);
        bindingResult = mock(BindingResult.class);
        modelMap = new ModelMap();

        when(messageUtil.getMessage(anyString())).thenReturn("Error message");
    }

    @Test
    void handleAuthSuccess_ShouldRedirectToUserInfo() {
        String result = authenticationController.handleAuthSuccess();
        assertEquals("redirect:/users/info", result);
    }

    @Test
    void showLoginPage_ShouldReturnLoginPage() {
        ModelAndView modelAndView = authenticationController.showLoginPage(modelMap);
        assertEquals("login-page", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("authenticationRequest"));
    }

    @Test
    void loginUser_WithValidCredentials_ShouldRedirectToUserInfo() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("user@example.com", "password");
        AuthenticationResponse response = new AuthenticationResponse("token");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(authenticationService.authenticate(request)).thenReturn(response);

        // Act
        ModelAndView result = authenticationController.loginUser(request, bindingResult, redirectAttributes, httpServletResponse, modelMap);

        // Assert
        assertEquals("redirect:/users/info", result.getViewName());
        verify(httpServletResponse).addCookie(any(Cookie.class));
    }

    @Test
    void loginUser_WithInvalidCredentials_ShouldReturnLoginPageWithError() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("user@example.com", "wrong");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(authenticationService.authenticate(request))
                .thenThrow(new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.WRONG_PASSWORD.getStatusMessage()));

        // Act
        ModelAndView result = authenticationController.loginUser(request, bindingResult, redirectAttributes, httpServletResponse, modelMap);

        // Assert
        assertEquals("login-page", result.getViewName());
        assertTrue(result.getModel().containsKey("error"));
    }

    @Test
    void loginUser_WithValidationErrors_ShouldReturnLoginPage() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("", "");

        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        ModelAndView result = authenticationController.loginUser(
                request, bindingResult, redirectAttributes, httpServletResponse, modelMap);

        // Assert
        assertEquals("login-page", result.getViewName());
    }

    @Test
    void showForgotPasswordPage_ShouldReturnForgotPasswordPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/forgot-password"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("forgot-password-page"));
    }

    @Test
    void processForgotPassword_WithValidEmail_ShouldRedirectWithSuccessMessage() throws Exception {
        // Arrange
        UserDTO user = new UserDTO();
        user.setEmail("user@example.com");

        when(userService.getUserDTOByEmail(eq("user@example.com"))).thenReturn(user);
        doNothing().when(passwordResetService).createPasswordResetTokenForUser(eq(user), anyString());

        // Act
        ModelAndView result = authenticationController.processForgotPassword("user@example.com", redirectAttributes);

        // Assert
        assertEquals("redirect:/forgot-password", result.getViewName());
        verify(redirectAttributes).addFlashAttribute(eq("message"), eq("reset.link"));
    }

    @Test
    void processForgotPassword_WithInvalidEmail_ShouldReturnPageWithError() {
        // Arrange
        when(userService.getUserDTOByEmail("nonexistent@example.com"))
                .thenThrow(new NotFoundException(StatusCodes.NOT_FOUND.name(), StatusMessages.USER_NOT_FOUND.getStatusMessage()));

        // Act
        ModelAndView result = authenticationController.processForgotPassword("nonexistent@example.com", redirectAttributes);

        // Assert
        assertEquals("forgot-password-page", result.getViewName());
        assertTrue(result.getModel().containsKey("error"));
    }

    @Test
    void showResetPage_WithValidToken_ShouldReturnResetPage() {
        // Arrange
        String validToken = UUID.randomUUID().toString();
        when(passwordResetService.validateToken(validToken)).thenReturn("valid");

        // Act
        ModelAndView result = authenticationController.showResetPage(validToken, modelMap, redirectAttributes);

        // Assert
        assertEquals("reset-password-page", result.getViewName());
        assertTrue(result.getModel().containsKey("token"));
    }

    @Test
    void showResetPage_WithInvalidToken_ShouldRedirectToLogin() {
        // Arrange
        String invalidToken = "invalid-token";
        when(passwordResetService.validateToken(invalidToken))
                .thenThrow(new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.LINK_EXPIRED.getStatusMessage()));

        // Act
        ModelAndView result = authenticationController.showResetPage(invalidToken, modelMap, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result.getViewName());
        verify(redirectAttributes).addFlashAttribute("message", "link.expired");
    }

    @Test
    void processReset_WithValidToken_ShouldRedirectToLogin() {
        // Arrange
        String validToken = UUID.randomUUID().toString();
        String newPassword = "newPassword123";

        doNothing().when(passwordResetService).resetPassword(validToken, newPassword);

        // Act
        ModelAndView result = authenticationController.processReset(validToken, newPassword, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result.getViewName());
        verify(redirectAttributes).addFlashAttribute("message", "password.updated");
    }

    @Test
    void processReset_WithInvalidToken_ShouldRedirectBackWithError() {
        // Arrange
        String invalidToken = "invalid-token";
        String newPassword = "newPassword123";

        doThrow(new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.LINK_EXPIRED.getStatusMessage()))
                .when(passwordResetService).resetPassword(invalidToken, newPassword);

        // Act
        ModelAndView result = authenticationController.processReset(invalidToken, newPassword, redirectAttributes);

        // Assert
        assertEquals("redirect:/reset-password?token=" + invalidToken, result.getViewName());
        verify(redirectAttributes).addFlashAttribute("error", "Error message");
    }
}
