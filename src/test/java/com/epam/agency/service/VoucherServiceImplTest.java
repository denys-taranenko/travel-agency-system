package com.epam.agency.service;

import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.mapper.VoucherMapper;
import com.epam.agency.model.Order;
import com.epam.agency.model.Voucher;
import com.epam.agency.model.enums.*;
import com.epam.agency.repository.OrderRepository;
import com.epam.agency.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    void createVoucher_ShouldReturnVoucherDTO() {
        //Arrange
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .title("Test Title")
                .price(BigDecimal.valueOf(100))
                .status("AVAILABLE")
                .hotStatus(false)
                .build();

        Voucher voucher = Voucher.builder()
                .title(voucherDTO.getTitle())
                .price(voucherDTO.getPrice())
                .status(VoucherStatus.AVAILABLE)
                .hotStatus(voucherDTO.getHotStatus())
                .build();

        Voucher savedVoucher = Voucher.builder()
                .id(UUID.randomUUID())
                .title(voucherDTO.getTitle())
                .price(voucherDTO.getPrice())
                .status(VoucherStatus.AVAILABLE)
                .hotStatus(voucherDTO.getHotStatus())
                .build();

        when(voucherMapper.toVoucher(voucherDTO)).thenReturn(voucher);
        when(voucherRepository.save(voucher)).thenReturn(savedVoucher);
        when(voucherMapper.toVoucherDTO(savedVoucher)).thenReturn(voucherDTO);

        //Act
        VoucherDTO result = voucherService.createVoucher(voucherDTO);

        //Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(voucherDTO.getTitle());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void updateVoucher_ShouldReturnUpdatedVoucherDTO() {
        //Arrange
        String voucherId = UUID.randomUUID().toString();
        VoucherDTO updatedVoucherDTO = VoucherDTO.builder()
                .title("Updated Title")
                .price(BigDecimal.valueOf(150))
                .tourType("HEALTH")
                .transferType("TRAIN")
                .hotelType("FIVE_STARS")
                .build();

        Voucher existingVoucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .title("Old Title")
                .price(BigDecimal.valueOf(100))
                .build();

        Voucher updatedVoucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .title(updatedVoucherDTO.getTitle())
                .price(updatedVoucherDTO.getPrice())
                .tourType(TourType.HEALTH)
                .transferType(TransferType.TRAIN)
                .hotelType(HotelType.FIVE_STARS)
                .build();

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(existingVoucher));
        when(voucherRepository.save(existingVoucher)).thenReturn(updatedVoucher);
        when(voucherMapper.toVoucherDTO(updatedVoucher)).thenReturn(updatedVoucherDTO);

        //Act
        VoucherDTO result = voucherService.updateVoucher(voucherId, updatedVoucherDTO);

        //Assert
        assertThat(result.getTitle()).isEqualTo(updatedVoucherDTO.getTitle());
        assertThat(result.getPrice()).isEqualTo(updatedVoucherDTO.getPrice());
        verify(voucherRepository, times(1)).findById(UUID.fromString(voucherId));
        verify(voucherRepository, times(1)).save(existingVoucher);
    }


    @Test
    void deleteVoucher_ShouldMarkVoucherAsDeleted() {
        //Arrange
        String voucherId = UUID.randomUUID().toString();
        Voucher existingVoucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .status(VoucherStatus.AVAILABLE)
                .build();

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(existingVoucher));

        //Act
        voucherService.deleteVoucher(voucherId);

        //Assert
        assertThat(existingVoucher.getStatus()).isEqualTo(VoucherStatus.DELETED);
        verify(voucherRepository, times(1)).softDelete(existingVoucher.getId());
    }

    @Test
    void restoreVoucher_ShouldRestoreVoucherToAvailable() {
        //Arrange
        String voucherId = UUID.randomUUID().toString();
        Voucher existingVoucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .status(VoucherStatus.DELETED)
                .build();

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(existingVoucher));

        //Act
        voucherService.restoreVoucher(voucherId);

        //Assert
        assertThat(existingVoucher.getStatus()).isEqualTo(VoucherStatus.AVAILABLE);
        assertThat(existingVoucher.getDeletedAt()).isNull();
        verify(voucherRepository, times(1)).save(existingVoucher);
    }

    @Test
    void changeVoucherStatus_ShouldUpdateVoucherStatusAndHotStatus() {
        //Arrange
        String voucherId = UUID.randomUUID().toString();
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .status("EXPIRED")
                .hotStatus(true)
                .build();

        Voucher existingVoucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .status(VoucherStatus.AVAILABLE)
                .hotStatus(false)
                .build();

        Voucher updatedVoucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .status(VoucherStatus.EXPIRED)
                .hotStatus(true)
                .build();

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(existingVoucher));
        when(voucherMapper.toVoucher(voucherDTO)).thenReturn(updatedVoucher);
        when(voucherRepository.save(existingVoucher)).thenReturn(updatedVoucher);
        when(voucherMapper.toVoucherDTO(updatedVoucher)).thenReturn(voucherDTO);

        //Act
        VoucherDTO result = voucherService.changeVoucherStatus(voucherId, voucherDTO);

        //Assert
        assertThat(result.getStatus()).isEqualTo(voucherDTO.getStatus());
        assertThat(result.getHotStatus()).isEqualTo(voucherDTO.getHotStatus());
        verify(voucherRepository, times(1)).save(existingVoucher);
    }

    @Test
    void getVoucherDTOById_ShouldReturnVoucherDTO() {
        //Arrange
        String voucherId = UUID.randomUUID().toString();
        Voucher voucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .title("Test Title")
                .build();

        VoucherDTO voucherDTO = VoucherDTO.builder()
                .title("Test Title")
                .build();

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        //Act
        VoucherDTO result = voucherService.getVoucherDTOById(voucherId);

        //Assert
        assertThat(result.getTitle()).isEqualTo(voucherDTO.getTitle());
        verify(voucherRepository, times(1)).findById(UUID.fromString(voucherId));
    }

    @Test
    void getVoucherById_ShouldReturnVoucher() {
        //Arrange
        String voucherId = UUID.randomUUID().toString();
        Voucher voucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .build();

        when(voucherRepository.findById(UUID.fromString(voucherId))).thenReturn(Optional.of(voucher));

        //Act
        Voucher result = voucherService.getVoucherById(voucherId);

        //Assert
        assertThat(result.getId()).isEqualTo(voucher.getId());
        verify(voucherRepository, times(1)).findById(UUID.fromString(voucherId));
    }

    @Test
    void getUserVouchers_ShouldReturnListOfVoucherDTOs() {
        //Arrange
        String userId = UUID.randomUUID().toString();
        Voucher voucher = Voucher.builder()
                .title("Test Voucher")
                .build();

        Order order = Order.builder()
                .voucher(voucher)
                .status(OrderStatus.PAID)
                .build();

        List<Order> orders = List.of(order);
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .title("Test Voucher")
                .build();

        when(orderRepository.findByUserId(UUID.fromString(userId))).thenReturn(orders);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        //Act
        List<VoucherDTO> result = voucherService.getUserVouchers(userId);

        //Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(voucherDTO.getTitle());
        verify(orderRepository, times(1)).findByUserId(UUID.fromString(userId));
    }

    @Test
    void getAllAvailableVouchers_ShouldReturnPageOfVoucherDTOs() {
        //Arrange
        String tourType = "ADVENTURE";
        String transferType = "TRAIN";
        BigDecimal minPrice = BigDecimal.valueOf(50);
        BigDecimal maxPrice = BigDecimal.valueOf(200);
        String hotelType = "FIVE_STARS";
        Boolean hotStatus = false;
        Pageable pageable = PageRequest.of(0, 10);

        Voucher voucher = Voucher.builder()
                .title("Test Voucher")
                .build();

        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher));

        when(voucherRepository.findAllAvailableVouchers(TourType.ADVENTURE, TransferType.TRAIN,
                minPrice, maxPrice, HotelType.FIVE_STARS, hotStatus, pageable)).thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(new VoucherDTO());

        //Act
        Page<VoucherDTO> result = voucherService.getAllAvailableVouchers(tourType, transferType, minPrice,
                maxPrice, hotelType, hotStatus, pageable);

        //Assert
        assertThat(result).hasSize(1);
        verify(voucherRepository, times(1)).findAllAvailableVouchers(TourType.ADVENTURE, TransferType.TRAIN,
                minPrice, maxPrice, HotelType.FIVE_STARS, hotStatus, pageable);
    }

    @Test
    void getVoucherCount_ShouldReturnCorrectVoucherCount() {
        //Arrange
        long expectedCount = 10;

        when(voucherRepository.count()).thenReturn(expectedCount);

        //Act
        long result = voucherService.getVoucherCount();

        //Assert
        assertThat(result).isEqualTo(expectedCount);
        verify(voucherRepository, times(1)).count();
    }
}
