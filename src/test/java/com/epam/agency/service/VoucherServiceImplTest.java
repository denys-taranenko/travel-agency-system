package com.epam.agency.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.Order;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.model.enums.*;
import com.epam.finaltask.repository.OrderRepository;
import com.epam.finaltask.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    @Test
    void createVoucher_SuccessTest() {
        // Given
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTitle("Summer Escape");
        voucherDTO.setDescription("Enjoy a summer escape.");
        voucherDTO.setPrice(BigDecimal.valueOf(500.0));
        voucherDTO.setTourType(TourType.ECO.name());
        voucherDTO.setTransferType(TransferType.BUS.name());
        voucherDTO.setHotelType(HotelType.TWO_STARS.name());
        voucherDTO.setStatus(VoucherStatus.AVAILABLE.name());
        voucherDTO.setArrivalDate(LocalDate.of(2025, 7, 15));
        voucherDTO.setEvictionDate(LocalDate.of(2025, 7, 20));
        voucherDTO.setHotStatus(false);

        Voucher voucher = Voucher.builder()
                .title(voucherDTO.getTitle())
                .description(voucherDTO.getDescription())
                .price(voucherDTO.getPrice())
                .tourType(TourType.valueOf(voucherDTO.getTourType()))
                .transferType(TransferType.valueOf(voucherDTO.getTransferType()))
                .status(VoucherStatus.valueOf(voucherDTO.getStatus()))
                .arrivalDate(voucherDTO.getArrivalDate())
                .evictionDate(voucherDTO.getEvictionDate())
                .hotStatus(voucherDTO.getHotStatus())
                .build();

        when(voucherMapper.toVoucher(any(VoucherDTO.class))).thenReturn(voucher);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        // When
        VoucherDTO createdVoucherDTO = voucherService.createVoucher(voucherDTO);

        // Then
        assertNotNull(createdVoucherDTO, "The created VoucherDTO should not be null");
        assertEquals(voucherDTO.getTitle(), createdVoucherDTO.getTitle(), "The title should match the input");

        verify(voucherRepository, times(1)).save(any(Voucher.class));
        verify(voucherMapper, times(1)).toVoucher(any(VoucherDTO.class));
        verify(voucherMapper, times(1)).toVoucherDTO(any(Voucher.class));
    }

    @Test
    void updateVoucher_VoucherExists_SuccessTest() {
        // Given
        String id = UUID.randomUUID().toString();

        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTitle("Updated Title");
        voucherDTO.setDescription("Updated Description");
        voucherDTO.setPrice(BigDecimal.valueOf(399.99));
        voucherDTO.setTourType(TourType.SAFARI.name());
        voucherDTO.setTransferType(TransferType.PRIVATE_CAR.name());
        voucherDTO.setHotelType(HotelType.FOUR_STARS.name());
        voucherDTO.setStatus(VoucherStatus.AVAILABLE.name());
        voucherDTO.setArrivalDate(LocalDate.now());
        voucherDTO.setEvictionDate(LocalDate.now().plusDays(5));
        voucherDTO.setHotStatus(true);

        Voucher existingVoucher = new Voucher();
        existingVoucher.setTitle("Existing Title");
        existingVoucher.setDescription("Existing Description");
        existingVoucher.setPrice(BigDecimal.valueOf(199.99));
        existingVoucher.setTourType(TourType.ECO);
        existingVoucher.setTransferType(TransferType.JEEPS);
        existingVoucher.setHotelType(HotelType.FOUR_STARS);
        existingVoucher.setStatus(VoucherStatus.AVAILABLE);
        existingVoucher.setArrivalDate(LocalDate.now());
        existingVoucher.setEvictionDate(LocalDate.now().plusDays(5));
        existingVoucher.setHotStatus(true);

        Voucher updatedVoucher = new Voucher();
        updatedVoucher.setTitle(voucherDTO.getTitle());
        updatedVoucher.setDescription(voucherDTO.getDescription());
        updatedVoucher.setTourType(TourType.valueOf(voucherDTO.getTourType()));
        updatedVoucher.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
        updatedVoucher.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
        updatedVoucher.setStatus(VoucherStatus.valueOf(voucherDTO.getStatus()));
        updatedVoucher.setArrivalDate(voucherDTO.getArrivalDate());
        updatedVoucher.setEvictionDate(voucherDTO.getEvictionDate());
        updatedVoucher.setHotStatus(Boolean.parseBoolean(String.valueOf(voucherDTO.getHotStatus())));

        when(voucherRepository.findById(UUID.fromString(id))).thenReturn(Optional.of(existingVoucher));
        when(voucherMapper.toVoucher(any(VoucherDTO.class))).thenReturn(updatedVoucher);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(updatedVoucher);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        // When
        VoucherDTO resultDTO = voucherService.updateVoucher(id, voucherDTO);

        // Then
        assertNotNull(resultDTO, "The returned VoucherDTO should not be null");
        assertEquals(voucherDTO.getTitle(), resultDTO.getTitle(), "The title should match the updated value");

        verify(voucherRepository, times(1)).findById(UUID.fromString(id));
        verify(voucherRepository, times(1)).save(any(Voucher.class));
        verify(voucherMapper, times(1)).toVoucher(any(VoucherDTO.class));
        verify(voucherMapper, times(1)).toVoucherDTO(any(Voucher.class));
    }

    @Test
    void updateVoucher_VoucherDoesNotExist_ThrowNotFoundExceptionTest() {
        // Given
        String id = UUID.randomUUID().toString();
        VoucherDTO voucherDTO = new VoucherDTO();

        when(voucherRepository.findById(UUID.fromString(id))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> voucherService.updateVoucher(id, voucherDTO),
                "Expected EntityNotFoundException to be thrown if the voucher is not found");

        verify(voucherRepository, times(1)).findById(UUID.fromString(id));
        verify(voucherRepository, never()).save(any(Voucher.class));
    }

    @Test
    void deleteVoucher_VoucherExists_SuccessTest() {
        // Given
        String voucherId = UUID.randomUUID().toString();

        // When
        voucherService.deleteVoucher(voucherId);

        // Then
        verify(voucherRepository, times(1)).deleteById(UUID.fromString(voucherId));
    }

    @Test
    void changeHotStatus_VoucherExists_SuccessTest() {
        // Given
        String id = UUID.randomUUID().toString();
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setHotStatus(true);
        voucherDTO.setStatus("AVAILABLE");

        Voucher existingVoucher = new Voucher();
        existingVoucher.setHotStatus(false);
        existingVoucher.setStatus(VoucherStatus.AVAILABLE);

        Voucher updatedVoucher = new Voucher();
        updatedVoucher.setHotStatus(Boolean.parseBoolean(String.valueOf(voucherDTO.getHotStatus())));
        updatedVoucher.setStatus(VoucherStatus.valueOf(voucherDTO.getStatus()));

        when(voucherRepository.findById(UUID.fromString(id))).thenReturn(Optional.of(existingVoucher));
        when(voucherMapper.toVoucher(any(VoucherDTO.class))).thenReturn(updatedVoucher);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(updatedVoucher);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        // When
        VoucherDTO resultDTO = voucherService.changeVoucherStatus(id, voucherDTO);

        // Then
        assertNotNull(resultDTO, "The returned VoucherDTO should not be null");
        assertTrue(Boolean.parseBoolean(String.valueOf(resultDTO.getHotStatus())), "The 'isHot' status should be true");
        assertEquals("AVAILABLE", resultDTO.getStatus(), "The status should be updated to AVAILABLE");

        verify(voucherRepository, times(1)).findById(UUID.fromString(id));
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void getVoucherDTOById_SuccessTest() {
        // Given
        String voucherId = UUID.randomUUID().toString();
        Voucher voucher = new Voucher();
        voucher.setId(UUID.fromString(voucherId));

        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setId(voucherId);

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        // When
        VoucherDTO result = voucherService.getVoucherDTOById(voucherId);

        // Then
        assertNotNull(result, "VoucherDTO should not be null");
        assertEquals(voucherDTO, result, "Returned VoucherDTO should match the expected value");

        verify(voucherRepository, times(1)).findById(UUID.fromString(voucherId));
        verify(voucherMapper, times(1)).toVoucherDTO(voucher);
    }

    @Test
    void getVoucherById_SuccessTest() {
        // Given
        String voucherId = UUID.randomUUID().toString();
        Voucher voucher = new Voucher();
        voucher.setId(UUID.fromString(voucherId));

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(voucher));

        // When
        Voucher result = voucherService.getVoucherById(voucherId);

        // Then
        assertNotNull(result, "Voucher should not be null");
        assertEquals(voucher, result, "Returned voucher should match the expected value");

        verify(voucherRepository, times(1)).findById(UUID.fromString(voucherId));
    }

    @Test
    void getVoucherById_NotFoundTest() {
        // Given
        String voucherId = UUID.randomUUID().toString();

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> voucherService.getVoucherById(voucherId),
                "Expected NotFoundException to be thrown when voucher not found");
    }

    @Test
    void getUserVouchers_SuccessTest() {
        // Given
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(UUID.fromString(userId));

        Voucher voucher = new Voucher();

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);

        order.setVoucher(voucher);
        List<Order> orders = List.of(order);

        when(orderRepository.findByUserId(UUID.fromString(userId))).thenReturn(orders);

        VoucherDTO voucherDTO = new VoucherDTO();
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        // When
        List<VoucherDTO> result = voucherService.getUserVouchers(userId);

        // Then
        assertNotNull(result, "Result list should not be null");
        assertEquals(1, result.size(), "The result list should contain one VoucherDTO");
        assertEquals(voucherDTO, result.get(0), "The VoucherDTO should match the expected value");

        verify(orderRepository, times(1)).findByUserId(UUID.fromString(userId));
        verify(voucherMapper, times(1)).toVoucherDTO(voucher);
    }

    @Test
    void getAllAvailableVouchers_SuccessTest() {
        // Given
        String tourType = "ECO";
        String transferType = "BUS";
        BigDecimal minPrice = BigDecimal.valueOf(100);
        BigDecimal maxPrice = BigDecimal.valueOf(1000);
        String hotelType = "THREE_STARS";
        Boolean hotStatus = false;
        Pageable pageable = PageRequest.of(0, 10);

        Voucher voucher = new Voucher();
        voucher.setTourType(TourType.ECO);
        voucher.setTransferType(TransferType.BUS);
        voucher.setPrice(BigDecimal.valueOf(500));
        voucher.setHotelType(HotelType.THREE_STARS);
        voucher.setHotStatus(false);

        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTourType("ECO");
        voucherDTO.setTransferType("BUS");
        voucherDTO.setPrice(BigDecimal.valueOf(500));
        voucherDTO.setHotelType("THREE_STARS");
        voucherDTO.setHotStatus(false);

        Page<Voucher> pageVouchers = new PageImpl<>(List.of(voucher));

        when(voucherRepository.findAllAvailableVouchers(any(), any(), eq(minPrice), eq(maxPrice), any(), eq(hotStatus), eq(pageable)))
                .thenReturn(pageVouchers);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        // When
        Page<VoucherDTO> result = voucherService.getAllAvailableVouchers(tourType, transferType, minPrice, maxPrice, hotelType, hotStatus, pageable);

        // Then
        assertNotNull(result, "Result page should not be null");
        assertEquals(1, result.getTotalElements(), "The result page should contain one element");
        assertEquals(voucherDTO, result.getContent().get(0), "The VoucherDTO should match the expected value");

        verify(voucherRepository, times(1)).findAllAvailableVouchers(any(), any(), eq(minPrice), eq(maxPrice), any(), eq(hotStatus), eq(pageable));
        verify(voucherMapper, times(1)).toVoucherDTO(any(Voucher.class));
    }

    @Test
    void getVoucherCount_SuccessTest() {
        // Given
        long expectedCount = 5L;
        when(voucherRepository.count()).thenReturn(expectedCount);

        // When
        long result = voucherService.getVoucherCount();

        // Then
        assertEquals(expectedCount, result, "The count should match the expected value");
        verify(voucherRepository, times(1)).count();
    }
}
