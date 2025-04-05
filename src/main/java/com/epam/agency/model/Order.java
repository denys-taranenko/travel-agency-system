package com.epam.agency.model;

import com.epam.agency.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "voucher"})
@EqualsAndHashCode(of = "id")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @Column(name = "order_price")
    private BigDecimal orderPrice;

    @Column(name = "voucher_arrival_date")
    private LocalDate voucherArrivalDate;

    @Column(name = "voucher_eviction_date")
    private LocalDate voucherEvictionDate;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "message")
    private String message;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "call_back")
    private boolean callBackRequest;

    @Column(name = "last_customer")
    private UUID lastCustomerId;
}
