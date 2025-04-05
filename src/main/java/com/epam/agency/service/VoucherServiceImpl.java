package com.epam.agency.service;

import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.exception.NotFoundException;
import com.epam.agency.exception.status.StatusCodes;
import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.mapper.VoucherMapper;
import com.epam.agency.model.Order;
import com.epam.agency.model.Voucher;
import com.epam.agency.model.enums.*;
import com.epam.agency.repository.OrderRepository;
import com.epam.agency.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final VoucherMapper voucherMapper;

    @Override
    public VoucherDTO createVoucher(VoucherDTO voucherDTO) {
        Voucher voucher = voucherMapper.toVoucher(voucherDTO);

        voucher.setStatus(VoucherStatus.AVAILABLE);
        voucher.setHotStatus(voucherDTO.getHotStatus());

        Voucher savedVoucher = voucherRepository.save(voucher);
        return voucherMapper.toVoucherDTO(savedVoucher);
    }

    @Override
    public VoucherDTO updateVoucher(String voucherId, VoucherDTO voucherDTO) {
        Voucher voucher = getVoucherById(voucherId);

        voucher.setTitle(voucherDTO.getTitle());
        voucher.setDescription(voucherDTO.getDescription());
        voucher.setPrice(voucherDTO.getPrice());
        voucher.setTourType(TourType.valueOf(voucherDTO.getTourType()));
        voucher.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
        voucher.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
        voucher.setArrivalDate(voucherDTO.getArrivalDate());
        voucher.setEvictionDate(voucherDTO.getEvictionDate());
        voucher.setHotStatus(voucherDTO.getHotStatus());

        if (voucher.getStatus() == VoucherStatus.EXPIRED) {
            voucher.setStatus(VoucherStatus.AVAILABLE);
        }

        Voucher updatedVoucher = voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(updatedVoucher);
    }

    @Transactional
    @Override
    public void deleteVoucher(String voucherId) {
        Voucher voucher = getVoucherById(voucherId);
        voucher.setStatus(VoucherStatus.DELETED);
        voucherRepository.softDelete(voucher.getId());
    }

    @Override
    public void restoreVoucher(String voucherId) {
        Voucher voucher = getVoucherById(voucherId);
        voucher.setStatus(VoucherStatus.AVAILABLE);
        voucher.setDeletedAt(null);
        voucherRepository.save(voucher);
    }

    @Override
    public VoucherDTO changeVoucherStatus(String voucherId, VoucherDTO voucherDTO) {
        Voucher voucher = getVoucherById(voucherId);
        Voucher changedVoucher = voucherMapper.toVoucher(voucherDTO);

        voucher.setHotStatus(changedVoucher.getHotStatus());
        voucher.setStatus(changedVoucher.getStatus());

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public VoucherDTO getVoucherDTOById(String voucherId) {
        Voucher voucher = getVoucherById(voucherId);
        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public Voucher getVoucherById(String voucherId) {
        return voucherRepository.findById(UUID.fromString(voucherId)).orElseThrow(() -> new NotFoundException(
                StatusCodes.NOT_FOUND.name(), StatusMessages.VOUCHER_NOT_FOUND.getStatusMessage()));
    }

    @Override
    public List<VoucherDTO> getUserVouchers(String userId) {
        List<Order> orders = orderRepository.findByUserId(UUID.fromString(userId));
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID)
                .map(Order::getVoucher)
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<VoucherDTO> getAllAvailableVouchers(String tourType, String transferType, BigDecimal minPrice,
                                                    BigDecimal maxPrice, String hotelType, Boolean hotStatus, Pageable pageable) {

        TourType parsedTourType = parseEnum(tourType, TourType.class);
        TransferType parsedTransferType = parseEnum(transferType, TransferType.class);
        HotelType parsedHotelType = parseEnum(hotelType, HotelType.class);

        Page<Voucher> vouchers = voucherRepository.findAllAvailableVouchers(parsedTourType, parsedTransferType,
                minPrice, maxPrice, parsedHotelType, hotStatus, pageable);
        return vouchers.map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> getAllVouchersForManagement(String tourType, String transferType, String hotelType,
                                                        Boolean hotStatus, String search, Pageable pageable) {

        TourType parsedTourType = parseEnum(tourType, TourType.class);
        TransferType parsedTransferType = parseEnum(transferType, TransferType.class);
        HotelType parsedHotelType = parseEnum(hotelType, HotelType.class);

        Page<Voucher> vouchers = voucherRepository.findAllAvailableVouchersForManagement(parsedTourType, parsedTransferType,
                parsedHotelType, hotStatus, search, pageable);
        return vouchers.map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> getAllArchiveVouchers(String tourType, String transferType, String hotelType, String search, Pageable pageable) {

        TourType parsedTourType = parseEnum(tourType, TourType.class);
        TransferType parsedTransferType = parseEnum(transferType, TransferType.class);
        HotelType parsedHotelType = parseEnum(hotelType, HotelType.class);

        Page<Voucher> vouchers = voucherRepository.findAllArchiveVouchers(parsedTourType, parsedTransferType,
                parsedHotelType, search, pageable);
        return vouchers.map(voucherMapper::toVoucherDTO);
    }

    @Override
    public long getVoucherCount() {
        return voucherRepository.count();
    }

    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumClass) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
