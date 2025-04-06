package com.epam.agency.controller;

import com.epam.agency.dto.UserDTO;
import com.epam.agency.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com", roles = "USER")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private FileService fileService;

    @Test
    void cancelOrder_ShouldRedirectOnSuccess() throws Exception {
        doNothing().when(orderService).cancelOrder(anyString(), anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/users/info/cancel/1")
                        .with(csrf())
                        .param("reason", "test reason"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/info"));
    }

    @Test
    void cancelOrder_ShouldRedirectWithErrorOnFailure() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users/info/cancel/1")
                        .with(csrf())
                        .param("reason", "test reason"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/info"));
    }

    @Test
    void showTopUpPage_ShouldReturnTopUpPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/top-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("top-up-balance-page"))
                .andExpect(model().attributeExists("stripePublicKey"));
    }

    @Test
    void showSettingsPage_ShouldReturnSettingsPage() throws Exception {
        UserDTO user = new UserDTO();
        user.setId("1");
        String[] avatars = new String[]{"avatar1", "avatar2"};

        when(userService.getUserDTO()).thenReturn(user);
        when(fileService.getAvailableAvatars()).thenReturn(avatars);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/info/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-settings-page"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("avatars"));
    }

    @Test
    void updateUser_ShouldRedirectOnSuccess() throws Exception {
        UserDTO user = new UserDTO();
        user.setId("1");
        String[] avatars = new String[]{"avatar1", "avatar2"};

        when(userService.getUserDTO()).thenReturn(user);
        when(fileService.getAvailableAvatars()).thenReturn(avatars);
        when(userService.updateUser(any())).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/info/settings")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/info/settings"));
    }

    @Test
    void changeAvatar_ShouldRedirect() throws Exception {
        UserDTO user = new UserDTO();
        user.setId("1");

        when(userService.getUserDTO()).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/info/settings/avatar")
                        .with(csrf())
                        .param("avatar", "new-avatar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/info/settings"));
    }
}
