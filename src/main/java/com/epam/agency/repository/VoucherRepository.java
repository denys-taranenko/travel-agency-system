package com.epam.agency.repository;

import com.epam.agency.model.Voucher;
import com.epam.agency.model.enums.HotelType;
import com.epam.agency.model.enums.TourType;
import com.epam.agency.model.enums.TransferType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

    @Modifying
    @Query("UPDATE Voucher v SET v.deletedAt = CURRENT_DATE WHERE v.id = :id")
    void softDelete(UUID id);

    @Query("SELECT v FROM Voucher v WHERE " +
            "(:tourType IS NULL OR v.tourType = :tourType) AND " +
            "(:transferType IS NULL OR v.transferType = :transferType) AND " +
            "(:minPrice IS NULL OR v.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR v.price <= :maxPrice) AND " +
            "(:hotelType IS NULL OR v.hotelType = :hotelType) AND " +
            "(:hotStatus IS NULL OR v.hotStatus = :hotStatus) AND " +
            "(v.status = 'AVAILABLE')")
    Page<Voucher> findAllAvailableVouchers(@Param("tourType") TourType tourType, @Param("transferType") TransferType transferType,
                                           @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
                                           @Param("hotelType") HotelType hotelType, @Param("hotStatus") Boolean hotStatus,
                                           Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE " +
            "(:tourType IS NULL OR v.tourType = :tourType) AND " +
            "(:transferType IS NULL OR v.transferType = :transferType) AND " +
            "(:hotelType IS NULL OR v.hotelType = :hotelType) AND " +
            "(:hotStatus IS NULL OR v.hotStatus = :hotStatus) AND " +
            "(v.status NOT IN ('DELETED', 'EXPIRED')) AND " +
            "(COALESCE(:search, '') = '' OR LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%'))) ")
    Page<Voucher> findAllAvailableVouchersForManagement(@Param("tourType") TourType tourType, @Param("transferType") TransferType transferType,
                                                        @Param("hotelType") HotelType hotelType, @Param("hotStatus") Boolean hotStatus,
                                                        @Param("search") String search, Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE " +
            "(:tourType IS NULL OR v.tourType = :tourType) AND " +
            "(:transferType IS NULL OR v.transferType = :transferType) AND " +
            "(:hotelType IS NULL OR v.hotelType = :hotelType) AND " +
            "(v.status IN ('DELETED', 'EXPIRED')) AND " +
            "(COALESCE(:search, '') = '' OR LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%'))) ")
    Page<Voucher> findAllArchiveVouchers(@Param("tourType") TourType tourType, @Param("transferType") TransferType transferType,
                                         @Param("hotelType") HotelType hotelType, @Param("search") String search, Pageable pageable);
}
