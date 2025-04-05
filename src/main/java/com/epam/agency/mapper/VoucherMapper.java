package com.epam.agency.mapper;

import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.model.Voucher;
import com.epam.agency.model.enums.HotelType;
import com.epam.agency.model.enums.TourType;
import com.epam.agency.model.enums.TransferType;
import com.epam.agency.model.enums.VoucherStatus;
import org.mapstruct.Mapper;

import java.util.UUID;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;

@Mapper(componentModel = "spring", imports = {UUID.class, TourType.class, TransferType.class, HotelType.class, VoucherStatus.class},
        injectionStrategy = CONSTRUCTOR)
public interface VoucherMapper {
    Voucher toVoucher(VoucherDTO voucherDTO);

    VoucherDTO toVoucherDTO(Voucher voucher);
}