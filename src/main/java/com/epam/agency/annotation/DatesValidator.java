package com.epam.agency.annotation;

import com.epam.agency.dto.VoucherDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DatesValidator implements ConstraintValidator<ValidDates, VoucherDTO> {

    @Override
    public boolean isValid(VoucherDTO dto, ConstraintValidatorContext context) {
        if (dto.getEvictionDate() == null || dto.getArrivalDate() == null) {
            return true;
        }
        if (dto.getEvictionDate().isBefore(dto.getArrivalDate())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{voucher.date.validator}")
                    .addPropertyNode("evictionDate")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
