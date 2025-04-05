package com.epam.agency.model;

import com.epam.agency.model.enums.HotelType;
import com.epam.agency.model.enums.TourType;
import com.epam.agency.model.enums.TransferType;
import com.epam.agency.model.enums.VoucherStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedFilter", condition = "deleted_at IS NULL")
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "tour_type", nullable = false)
    private TourType tourType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "transfer_type", nullable = false)
    private TransferType transferType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "hotel_type", nullable = false)
    private HotelType hotelType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VoucherStatus status;

    @Column(name = "arrival_date", nullable = false)
    private LocalDate arrivalDate;

    @Column(name = "eviction_date", nullable = false)
    private LocalDate evictionDate;

    @Column(name = "hot_status")
    private Boolean hotStatus;

    @Column(name = "deleted_at")
    private LocalDate deletedAt;
}
