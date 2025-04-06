package com.epam.agency.service;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.dto.UserDTO;
import com.epam.agency.dto.VoucherDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private VoucherService voucherService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    void generateOrderPdfTest() throws Exception {
        //Arrange
        String orderId = "123";
        OrderDTO order = new OrderDTO();
        order.setUserId("1");
        order.setVoucherId("v1");

        UserDTO user = new UserDTO();
        user.setId("1");

        VoucherDTO voucher = new VoucherDTO();
        voucher.setId("v1");

        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(userService.getUserDTOById(order.getUserId())).thenReturn(user);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>PDF Content</html>");

        MockHttpServletResponse response = new MockHttpServletResponse();

        //Act
        fileService.generateOrderPdf(orderId, response);

        //Assert
        assertEquals(MediaType.APPLICATION_PDF_VALUE, response.getContentType());
        assertEquals("inline; filename=order_123.pdf", response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertNotNull(response.getOutputStream());
    }

    @Test
    void generateOrderPdfThrowsExceptionTest() {
        String orderId = "123";
        when(orderService.getOrderById(orderId)).thenThrow(new RuntimeException("Order not found"));
        assertThrows(RuntimeException.class, () -> fileService.generateOrderPdf(orderId, response));
    }

    @Test
    void getAvailableAvatarsTest() {
        //Arrange
        File avatarsDir = mock(File.class);
        when(avatarsDir.list(any())).thenReturn(new String[]{"cat.png", "bear.png", "default-avatar.png", "dog.png", "rabbit.png"});

        FileServiceImpl fileService = new FileServiceImpl(templateEngine, orderService, userService, voucherService) {

            //Act
            @Override
            public String[] getAvailableAvatars() {
                return avatarsDir.list((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
            }
        };

        String[] avatars = fileService.getAvailableAvatars();

        //Assert
        assertNotNull(avatars);
        assertEquals(5, avatars.length);
        assertTrue(avatars[0].endsWith(".png") || avatars[0].endsWith(".jpg"));
        assertTrue(avatars[1].endsWith(".png") || avatars[1].endsWith(".jpg"));
        assertTrue(avatars[2].endsWith(".png") || avatars[2].endsWith(".jpg"));
        assertTrue(avatars[3].endsWith(".png") || avatars[3].endsWith(".jpg"));
        assertTrue(avatars[4].endsWith(".png") || avatars[4].endsWith(".jpg"));
    }
}
