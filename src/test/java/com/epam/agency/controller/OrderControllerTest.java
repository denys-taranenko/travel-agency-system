package com.epam.agency.controller;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.dto.UserDTO;
import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.exception.*;
import com.epam.agency.model.enums.OrderStatus;
import com.epam.agency.service.*;
import com.epam.agency.util.MessageUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageUtil messageUtil;

    @MockBean
    private FileService fileService;

    @Autowired
    private OrderController orderController;

    private OrderDTO testOrder;
    private VoucherDTO testVoucher;

    @BeforeEach
    void setUp() {
        testOrder = new OrderDTO();
        testOrder.setId("a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8");
        testOrder.setUserId("user-1");
        testOrder.setVoucherId("voucher-1");
        testOrder.setOrderPrice(BigDecimal.valueOf(1000));
        testOrder.setStatus(OrderStatus.REGISTERED.name());

        testVoucher = new VoucherDTO();
        testVoucher.setId("voucher-1");
        testVoucher.setTitle("Test Voucher");
        testVoucher.setPrice(BigDecimal.valueOf(1000));

        UserDTO testUser = new UserDTO();
        testUser.setId("user-1");
        testUser.setBalance(BigDecimal.valueOf(2000));

        when(messageUtil.getMessage(anyString())).thenReturn("Error message");
        when(userService.getUserDTO()).thenReturn(testUser);
    }

    @Test
    @WithMockUser
    void showOrderPage_ShouldReturnOrderCreationPage() {
        when(voucherService.getVoucherDTOById("voucher-1")).thenReturn(testVoucher);

        ModelAndView mav = orderController.showOrderPage("voucher-1", new ModelMap());

        assertEquals("order-create-page", mav.getViewName());
        assertNotNull(mav.getModel().get("order"));
        assertEquals(testVoucher, mav.getModel().get("voucher"));
    }

    @Test
    @WithMockUser
    void createOrder_WithValidData_ShouldRedirect() {
        when(voucherService.getVoucherDTOById("voucher-1")).thenReturn(testVoucher);
        doNothing().when(orderService).createOrder(any(OrderDTO.class));

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setVoucherId("voucher-1");
        orderDTO.setUserId("user-1");

        ModelAndView mav = orderController.createOrder(
                "voucher-1", orderDTO, mock(RedirectAttributes.class), new ModelMap());

        assertEquals("redirect:/users/info", mav.getViewName());
    }

    @Test
    @WithMockUser
    void createOrder_WhenOrderExists_ShouldReturnError() {
        when(voucherService.getVoucherDTOById("voucher-1")).thenReturn(testVoucher);
        doThrow(new AlreadyExistsException("CONFLICT", "Voucher already ordered"))
                .when(orderService).createOrder(any(OrderDTO.class));

        ModelAndView mav = orderController.createOrder(
                "voucher-1", testOrder, mock(RedirectAttributes.class), new ModelMap());

        assertEquals("order-create-page", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
    }

    @Test
    @WithMockUser
    void generateOrderPdf_ShouldGeneratePdf() throws Exception {
        doNothing().when(fileService).generateOrderPdf(eq("order-1"), any(HttpServletResponse.class));

        orderController.generateOrderPdf("order-1", mock(HttpServletResponse.class));

        verify(fileService).generateOrderPdf(eq("order-1"), any(HttpServletResponse.class));
    }

    @Test
    @WithMockUser
    void showPaymentPage_ShouldReturnPaymentPage() {
        when(orderService.getOrderById("order-1")).thenReturn(testOrder);

        String viewName = orderController.showPaymentPage("order-1", new ModelMap());

        assertEquals("payment-page", viewName);
        verify(orderService).getOrderById("order-1");
    }

    @Test
    @WithMockUser
    void payOrder_WithValidToken_ShouldRedirect() {
        doNothing().when(orderService).payOrder("order-1", "valid-token");

        ModelAndView mav = orderController.payOrder(
                "order-1", "valid-token", mock(RedirectAttributes.class));

        assertEquals("redirect:/users/info", mav.getViewName());
        verify(orderService).payOrder("order-1", "valid-token");
    }

    @Test
    @WithMockUser
    void payOrder_WhenNotEnoughFunds_ShouldShowError() {
        doThrow(new InvalidDataException("NOT_FOUND", "Not enough funds"))
                .when(orderService).payOrder(eq("order-1"), eq("invalid-token"));

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        ModelAndView mav = orderController.payOrder(
                "order-1", "invalid-token", redirectAttributes);

        assertEquals("redirect:/order/pay/order-1", mav.getViewName());
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @WithMockUser
    void deleteOrder_ShouldRedirect() {
        doNothing().when(orderService).deleteOrder("order-1");

        ModelAndView mav = orderController.deleteOrder("order-1", mock(RedirectAttributes.class));

        assertEquals("redirect:/users/info", mav.getViewName());
        verify(orderService).deleteOrder("order-1");
    }

    @Test
    @WithMockUser
    void deleteOrder_WhenInvalidState_ShouldShowError() {
        doThrow(new InvalidDataException("BAD_REQUEST", "Cannot delete order"))
                .when(orderService).deleteOrder(eq("order-1"));

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        ModelAndView mav = orderController.deleteOrder("order-1", redirectAttributes);

        assertEquals("redirect:/users/info", mav.getViewName());
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @WithMockUser
    void testIntegration_CreateOrderPage() throws Exception {
        when(voucherService.getVoucherDTOById("voucher-1")).thenReturn(testVoucher);

        mockMvc.perform(get("/order/create/voucher-1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("order-create-page"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("voucher"));
    }

    @Test
    @WithMockUser
    void testIntegration_CreateOrder() throws Exception {
        when(voucherService.getVoucherDTOById("voucher-1")).thenReturn(testVoucher);
        doNothing().when(orderService).createOrder(any(OrderDTO.class));

        mockMvc.perform(post("/order/create/voucher-1")
                        .param("voucherId", "voucher-1")
                        .param("userId", "user-1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/info"));
    }

    @Test
    @WithMockUser
    void testIntegration_PaymentPage() throws Exception {
        when(orderService.getOrderById("order-1")).thenReturn(testOrder);

        mockMvc.perform(get("/order/pay/order-1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-page"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("stripePublicKey"));
    }
}
