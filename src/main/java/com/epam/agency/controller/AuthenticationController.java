package com.epam.agency.controller;

import com.epam.agency.auth.AuthenticationRequest;
import com.epam.agency.auth.AuthenticationResponse;
import com.epam.agency.auth.AuthenticationService;
import com.epam.agency.dto.UserDTO;
import com.epam.agency.exception.AccessDeniedException;
import com.epam.agency.exception.AlreadyExistsException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.exception.NotFoundException;
import com.epam.agency.security.PasswordResetService;
import com.epam.agency.service.UserService;
import com.epam.agency.util.MessageUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class AuthenticationController {
    private final MessageUtil messageUtil;
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;
    private final UserService userService;

    private static final String COOKIE = "TOKEN";

    @GetMapping("/auth/success")
    public String handleAuthSuccess() {
        return "redirect:/users/info";
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage(ModelMap model) {
        model.addAttribute("authenticationRequest", new AuthenticationRequest());
        return new ModelAndView("login-page", model);
    }

    @PostMapping("/login")
    public ModelAndView loginUser(@ModelAttribute("authenticationRequest") @Valid AuthenticationRequest authenticationRequest,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  HttpServletResponse httpServletResponse,
                                  ModelMap model) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("login-page", model);
        }

        try {
            AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);

            Cookie jwtCookie = setCookie(authenticationResponse);
            httpServletResponse.addCookie(jwtCookie);

            return new ModelAndView("redirect:/users/info");
        } catch (InvalidDataException | NotFoundException exception) {
            model.addAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("login-page", model);
        } catch (AccessDeniedException exception) {
            redirectAttributes.addFlashAttribute("message", "blocked");
            return new ModelAndView("redirect:/login");
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password-page";
    }

    @PostMapping("/forgot-password")
    public ModelAndView processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            UserDTO user = userService.getUserDTOByEmail(email);
            String token = UUID.randomUUID().toString();
            passwordResetService.createPasswordResetTokenForUser(user, token);

            redirectAttributes.addFlashAttribute("message", "reset.link");
            return new ModelAndView("redirect:/forgot-password");
        } catch (NotFoundException | AlreadyExistsException exception) {
            return new ModelAndView("forgot-password-page").addObject("error", messageUtil.getMessage(exception.getStatusMessage()));
        } catch (MessagingException exception) {
            throw new RuntimeException(exception);
        }
    }

    @GetMapping("/reset-password")
    public ModelAndView showResetPage(@RequestParam String token, ModelMap model, RedirectAttributes redirectAttributes) {
        try {
            String result = passwordResetService.validateToken(token);

            if (!result.equals("valid")) {
                model.addAttribute("message", "link.expired");
                return new ModelAndView("redirect:/login", model);
            }

            model.addAttribute("token", token);
            return new ModelAndView("reset-password-page", model);
        } catch (InvalidDataException exception) {
            redirectAttributes.addFlashAttribute("message", "link.expired");
            return new ModelAndView("redirect:/login", model);
        }
    }

    @PostMapping("/reset-password")
    public ModelAndView processReset(@RequestParam String token, @RequestParam String password, RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("message", "password.updated");
            return new ModelAndView("redirect:/login");
        } catch (InvalidDataException exception) {
            redirectAttributes.addFlashAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("redirect:/reset-password?token=" + token);
        }
    }

    private Cookie setCookie(AuthenticationResponse authenticationResponse) {
        Cookie cookie = new Cookie(COOKIE, authenticationResponse.getAccessToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setDomain("localhost");
        cookie.setAttribute("SameSite", "None");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        return cookie;
    }
}
