package com.epam.agency.controller;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.model.enums.*;
import com.epam.agency.service.OrderService;
import com.epam.agency.service.VoucherService;
import com.epam.agency.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private MessageUtil messageUtil;

    @Autowired
    private ManagementController managementController;

    private VoucherDTO testVoucher;
    private OrderDTO testOrder;

    @BeforeEach
    void setUp() {
        testVoucher = new VoucherDTO();
        testVoucher.setId("voucher-1");
        testVoucher.setTitle("Test Voucher");
        testVoucher.setTourType(TourType.HEALTH.name());
        testVoucher.setStatus(VoucherStatus.AVAILABLE.name());

        testOrder = new OrderDTO();
        testOrder.setId("order-1");
        testOrder.setStatus(OrderStatus.REGISTERED.name());

        when(messageUtil.getMessage(anyString())).thenReturn("Error message");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showAdminPanel_WithAdminRole_ShouldReturnManagementPanel() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/management"))
                .andExpect(status().isOk())
                .andExpect(view().name("management-panel-page"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void showAdminPanel_WithUserRole_ShouldDenyAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/management"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showVouchers_ShouldReturnVouchersPage() throws Exception {
        Page<VoucherDTO> voucherPage = new PageImpl<>(List.of(testVoucher));
        when(voucherService.getAllVouchersForManagement(any(), any(), any(), any(), any(), any()))
                .thenReturn(voucherPage);

        ModelAndView mav = managementController.showVouchers(
                null, null, null, null, null,
                "hotStatus", "desc", PageRequest.of(0, 7), new ModelMap());

        assertEquals("management-panel-vouchers-page", mav.getViewName());
        assertNotNull(mav.getModel().get("voucherPage"));
        assertNotNull(mav.getModel().get("tourTypes"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showArchive_ShouldReturnArchivePage() throws Exception {
        Page<VoucherDTO> voucherPage = new PageImpl<>(List.of(testVoucher));
        when(voucherService.getAllArchiveVouchers(any(), any(), any(), any(), any()))
                .thenReturn(voucherPage);

        ModelAndView mav = managementController.showArchive(
                null, null, null, null,
                "status", "desc", PageRequest.of(0, 7), new ModelMap());

        assertEquals("management-panel-archive-vouchers-page", mav.getViewName());
        assertNotNull(mav.getModel().get("voucherPage"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showCreateVoucherPage_ShouldReturnCreatePage() throws Exception {
        ModelAndView mav = managementController.showCreateVoucherPage(new ModelMap());

        assertEquals("create-voucher-page", mav.getViewName());
        assertNotNull(mav.getModel().get("voucher"));
        assertNotNull(mav.getModel().get("tourTypes"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveVoucher_WithValidData_ShouldRedirect() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.createVoucher(any())).thenReturn(testVoucher);

        ModelAndView mav = managementController.saveVoucher(
                testVoucher, bindingResult, mock(RedirectAttributes.class), new ModelMap());

        assertEquals("redirect:/management", mav.getViewName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveVoucher_WithInvalidData_ShouldReturnToForm() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        ModelAndView mav = managementController.saveVoucher(
                testVoucher, bindingResult, mock(RedirectAttributes.class), new ModelMap());

        assertEquals("create-voucher-page", mav.getViewName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditVoucherPage_ShouldReturnEditPage() throws Exception {
        when(voucherService.getVoucherDTOById("voucher-1")).thenReturn(testVoucher);

        ModelAndView mav = managementController.showEditVoucherPage("voucher-1", new ModelMap());

        assertEquals("edit-voucher-page", mav.getViewName());
        assertEquals(testVoucher, mav.getModel().get("voucher"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editVoucher_WithValidData_ShouldRedirect() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.updateVoucher(anyString(), any())).thenReturn(testVoucher);

        ModelAndView mav = managementController.editVoucher(
                testVoucher, bindingResult, "voucher-1", mock(RedirectAttributes.class), new ModelMap());

        assertTrue(Objects.requireNonNull(mav.getViewName()).startsWith("redirect:/vouchers/info/"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteVoucher_ShouldRedirect() throws Exception {
        String result = managementController.deleteVoucher("voucher-1", mock(RedirectAttributes.class));
        assertEquals("redirect:/management/vouchers", result);
        verify(voucherService).deleteVoucher("voucher-1");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void restoreVoucher_ShouldRedirect() throws Exception {
        String result = managementController.restoreVoucher("voucher-1", mock(RedirectAttributes.class));
        assertEquals("redirect:/management/vouchers", result);
        verify(voucherService).restoreVoucher("voucher-1");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showRequestsPage_ShouldReturnRequestsPage() throws Exception {
        Page<OrderDTO> orderPage = new PageImpl<>(List.of(testOrder));
        when(orderService.getAllOrders(any(), any())).thenReturn(orderPage);

        ModelAndView mav = managementController.showRequestsPage(
                null, "status", "desc", PageRequest.of(0, 7), new ModelMap());

        assertEquals("order-requests-page", mav.getViewName());
        assertNotNull(mav.getModel().get("orderPage"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showChangeVoucherPage_ShouldReturnChangePage() throws Exception {
        when(voucherService.getVoucherDTOById("voucher-1")).thenReturn(testVoucher);

        ModelAndView mav = managementController.showChangeVoucherPage("voucher-1", new ModelMap());

        assertEquals("change-voucher-page", mav.getViewName());
        assertEquals(testVoucher, mav.getModel().get("voucher"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeVoucherStatus_WithValidData_ShouldRedirect() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.changeVoucherStatus(anyString(), any())).thenReturn(testVoucher);

        ModelAndView mav = managementController.changeVoucherStatus(
                testVoucher, bindingResult, "voucher-1", mock(RedirectAttributes.class), new ModelMap());

        assertTrue(Objects.requireNonNull(mav.getViewName()).startsWith("redirect:/vouchers/info/"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRequest_ShouldRedirect() throws Exception {
        String result = managementController.approveRequest("order-1", mock(RedirectAttributes.class));
        assertEquals("redirect:/management/requests", result);
        verify(orderService).approveRequest("order-1");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelRequest_ShouldRedirect() throws Exception {
        String result = managementController.cancelRequest("order-1", mock(RedirectAttributes.class));
        assertEquals("redirect:/management/requests", result);
        verify(orderService).cancelRequest("order-1");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testIntegration_ManagementEndpoints() throws Exception {
        // Тест интеграции с MockMvc
        when(voucherService.getAllVouchersForManagement(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.get("/management/vouchers")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("management-panel-vouchers-page"));
    }
}
