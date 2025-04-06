package com.epam.agency.controller;

import com.epam.agency.dto.UserDTO;
import com.epam.agency.exception.AlreadyExistsException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.service.UserService;
import com.epam.agency.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageUtil messageUtil;

    @Autowired
    private PageController pageController;

    private UserDTO testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");

        when(messageUtil.getMessage(anyString())).thenReturn("Error message");
    }

    @Test
    void showMainPage_ShouldReturnMainPage() {
        String result = pageController.showMainPage();
        assertEquals("main-page", result);
    }

    @Test
    void showContactsPage_ShouldReturnContactsPage() {
        String result = pageController.showContactsPage();
        assertEquals("contacts-page", result);
    }

    @Test
    void showAboutUsPage_ShouldReturnAboutUsPage() {
        String result = pageController.showAboutUsPage();
        assertEquals("about-us-page", result);
    }

    @Test
    void showRegisterPage_ShouldReturnRegisterPageWithUserModel() {
        ModelAndView mav = pageController.showRegisterPage(new ModelMap());

        assertEquals("register-page", mav.getViewName());
        assertNotNull(mav.getModel().get("user"));
        assertInstanceOf(UserDTO.class, mav.getModel().get("user"));
    }

    @Test
    void registerUser_WithValidData_ShouldRedirectToLogin() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView mav = pageController.registerUser(
                testUser,
                bindingResult,
                mock(RedirectAttributes.class),
                new ModelMap());

        assertEquals("redirect:/login", mav.getViewName());
        verify(userService).registerUser(any(UserDTO.class));
    }

    @Test
    void registerUser_WithValidationErrors_ShouldReturnToRegisterPage() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        ModelAndView mav = pageController.registerUser(
                testUser, bindingResult, mock(RedirectAttributes.class), new ModelMap());

        assertEquals("register-page", mav.getViewName());
        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_WhenUserExists_ShouldShowError() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new AlreadyExistsException("CONFLICT", "User already exists"))
                .when(userService).registerUser(any(UserDTO.class));

        ModelAndView mav = pageController.registerUser(
                testUser, bindingResult, mock(RedirectAttributes.class), new ModelMap());

        assertEquals("register-page", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
    }

    @Test
    void registerUser_WithInvalidData_ShouldShowError() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new InvalidDataException("BAD_REQUEST", "Invalid user data"))
                .when(userService).registerUser(any(UserDTO.class));

        ModelAndView mav = pageController.registerUser(
                testUser, bindingResult, mock(RedirectAttributes.class), new ModelMap());

        assertEquals("register-page", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
    }

    @Test
    void testIntegration_MainPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("main-page"));
    }

    @Test
    void testIntegration_RegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register-page"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void testIntegration_RegisterUserSuccess() throws Exception {
        mockMvc.perform(post("/register")
                        .flashAttr("user", new UserDTO()))
                .andExpect(status().isOk())
                .andExpect(view().name("register-page"));
    }
}
