package com.epam.agency.dto;

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
public class OrderDTO {
    private String id;
    private String userId;
    private String voucherId;
    private BigDecimal orderPrice;
    private LocalDate voucherArrivalDate;
    private LocalDate voucherEvictionDate;
    private String status;
    private LocalDate orderDate;

    @Length(max = 254, message = "{order.length}")
    private String message;

    @Length(max = 254, message = "{order.length}")
    private String cancelReason;

    private boolean callBackRequest;

    private String lastCustomerId;
}
