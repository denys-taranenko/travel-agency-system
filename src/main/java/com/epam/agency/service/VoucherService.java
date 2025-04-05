package com.epam.agency.service;

import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface VoucherService {
    VoucherDTO createVoucher(VoucherDTO voucherDTO);

    VoucherDTO updateVoucher(String voucherId, VoucherDTO voucherDTO);

    void deleteVoucher(String voucherId);

    void restoreVoucher(String voucherId);

    VoucherDTO changeVoucherStatus(String voucherId, VoucherDTO voucherDTO);

    VoucherDTO getVoucherDTOById(String voucherId);

    Voucher getVoucherById(String voucherId);

    List<VoucherDTO> getUserVouchers(String userId);

    Page<VoucherDTO> getAllAvailableVouchers(String tourType, String transferType, BigDecimal minPrice,
                                             BigDecimal maxPrice, String hotelType, Boolean hotStatus, Pageable pageable);

    Page<VoucherDTO> getAllVouchersForManagement(String tourType, String transferType, String hotelType, Boolean hotStatus,
                                                 String search, Pageable pageable);

    Page<VoucherDTO> getAllArchiveVouchers(String tourType, String transferType, String hotelType, String search, Pageable pageable);

    long getVoucherCount();
}
