package com.epam.agency.dto;

import com.epam.agency.annotation.ValidDates;
import com.epam.agency.dto.group.OnChange;
import com.epam.agency.dto.group.OnCreate;
import com.epam.agency.dto.group.OnUpdate;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ValidDates(groups = {OnCreate.class, OnUpdate.class})
public class VoucherDTO {
    private String id;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.title.not.blank}")
    @Length(max = 254, groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.title.length}")
    private String title;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.description.not.blank}")
    @Length(max = 254, groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.description.length}")
    private String description;

    @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.price.not.null}")
    @PositiveOrZero(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.price.positive}")
    private BigDecimal price;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.tour.type.not.blank}")
    private String tourType;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.transfer.type.not.blank}")
    private String transferType;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.hotel.type.not.blank}")
    private String hotelType;

    private String status;

    @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.arrival.date.not.null}")
    @FutureOrPresent(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.arrival.date.future}")
    private LocalDate arrivalDate;

    @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.eviction.date.not.null}")
    @FutureOrPresent(groups = {OnCreate.class, OnUpdate.class}, message = "{voucher.eviction.date.future}")
    private LocalDate evictionDate;

    @NotNull(groups = {OnChange.class, OnCreate.class}, message = "{voucher.hot.status.not.null}")
    private Boolean hotStatus;
}
