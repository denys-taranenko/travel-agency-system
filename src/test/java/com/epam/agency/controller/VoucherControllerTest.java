package com.epam.agency.controller;

import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.model.enums.HotelType;
import com.epam.agency.model.enums.TourType;
import com.epam.agency.model.enums.TransferType;
import com.epam.agency.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @Test
    void showVouchersPage_ShouldReturnVouchersPage() throws Exception {
        Page<VoucherDTO> page = getVoucherDTOS();

        when(voucherService.getAllAvailableVouchers(any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/vouchers"))
                .andExpect(status().isOk())
                .andExpect(view().name("vouchers-page"))
                .andExpect(model().attributeExists("voucherPage"))
                .andExpect(model().attributeExists("tourTypes"))
                .andExpect(model().attributeExists("transferTypes"))
                .andExpect(model().attributeExists("hotelTypes"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    void showVouchersPage_WithFilters_ShouldReturnFilteredVouchers() throws Exception {
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setId("1");
        voucherDTO.setTourType(TourType.ADVENTURE.name());
        voucherDTO.setTransferType(TransferType.BUS.name());
        voucherDTO.setHotelType(HotelType.FOUR_STARS.name());
        voucherDTO.setPrice(BigDecimal.valueOf(1000));
        voucherDTO.setHotStatus(true);

        Page<VoucherDTO> page = new PageImpl<>(Collections.singletonList(voucherDTO));
        when(voucherService.getAllAvailableVouchers(anyString(), anyString(), any(), any(), anyString(), anyBoolean(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/vouchers")
                        .param("tourType", "BEACH")
                        .param("transferType", "BUS")
                        .param("minPrice", "500")
                        .param("maxPrice", "1500")
                        .param("hotelType", "FOUR_STARS")
                        .param("hotStatus", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("vouchers-page"))
                .andExpect(model().attributeExists("voucherPage"))
                .andExpect(model().attribute("tourType", "BEACH"))
                .andExpect(model().attribute("transferType", "BUS"))
                .andExpect(model().attribute("minPrice", BigDecimal.valueOf(500)))
                .andExpect(model().attribute("maxPrice", BigDecimal.valueOf(1500)))
                .andExpect(model().attribute("hotelType", "FOUR_STARS"))
                .andExpect(model().attribute("hotStatus", true));
    }

    @Test
    void showVoucherInfoPage_ShouldReturnVoucherInfoPage() throws Exception {
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setId("1");
        voucherDTO.setTourType(TourType.ADVENTURE.name());
        voucherDTO.setTransferType(TransferType.BUS.name());
        voucherDTO.setHotelType(HotelType.FOUR_STARS.name());
        voucherDTO.setPrice(BigDecimal.valueOf(1000));
        voucherDTO.setHotStatus(true);

        when(voucherService.getVoucherDTOById("1")).thenReturn(voucherDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/vouchers/info/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("voucher-info-page"))
                .andExpect(model().attributeExists("voucher"))
                .andExpect(model().attribute("voucher", voucherDTO));
    }

    private static Page<VoucherDTO> getVoucherDTOS() {
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setId("1");
        voucherDTO.setTourType(TourType.ADVENTURE.name());
        voucherDTO.setTransferType(TransferType.BUS.name());
        voucherDTO.setHotelType(HotelType.FOUR_STARS.name());
        voucherDTO.setPrice(BigDecimal.valueOf(1000));
        voucherDTO.setHotStatus(true);

        return new PageImpl<>(
                Collections.singletonList(voucherDTO),
                PageRequest.of(0, 3),
                1);
    }
}
