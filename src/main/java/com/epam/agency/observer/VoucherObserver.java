package com.epam.agency.observer;

import com.epam.agency.model.Voucher;
import com.epam.agency.model.enums.VoucherStatus;
import com.epam.agency.repository.VoucherRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VoucherObserver {
    private final VoucherRepository voucherRepository;

    @PostConstruct
    public void init() {
        updateVoucherStatuses();
        updateVoucherHotStatuses();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateVoucherStatuses() {
        List<Voucher> allVouchers = voucherRepository.findAll();
        for (Voucher voucher : allVouchers) {
            if (voucher.getEvictionDate().isBefore(LocalDate.now()) && voucher.getStatus() != VoucherStatus.EXPIRED) {
                voucher.setStatus(VoucherStatus.EXPIRED);
                voucherRepository.save(voucher);
            }
        }
    }

    /**
     * This method is scheduled to run once a day at midnight to update the hot status of vouchers
     * based on their arrival date. If a voucher's arrival date is within one month from the current date,
     * its hot status will be set to true.
     *
     * If your company has a policy of decreasing the voucher price as the check-in date approaches,
     * you can apply a discount by using the code below.
     * The discount is calculated as 15% of the voucher price.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateVoucherHotStatuses() {
        LocalDate currentDate = LocalDate.now();
        LocalDate dateOneMonthLater = currentDate.plusMonths(1);
        List<Voucher> allVouchers = voucherRepository.findAll();

        for (Voucher voucher : allVouchers) {
            if (voucher.getArrivalDate() != null && !voucher.getArrivalDate().isBefore(currentDate) && voucher.getArrivalDate().isBefore(dateOneMonthLater)) {

                // Uncomment the lines below to apply a 15% discount to the voucher price
                //BigDecimal voucherPrice = voucher.getPrice();
                //BigDecimal discount = voucherPrice.multiply(BigDecimal.valueOf(0.15));
                //BigDecimal newPrice = voucherPrice.subtract(discount);
                //voucher.setPrice(newPrice);

                voucher.setHotStatus(true);
                voucherRepository.save(voucher);
            }
        }
    }
}
