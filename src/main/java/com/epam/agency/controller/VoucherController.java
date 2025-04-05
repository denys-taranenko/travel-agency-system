package com.epam.agency.controller;

import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.model.enums.HotelType;
import com.epam.agency.model.enums.TourType;
import com.epam.agency.model.enums.TransferType;
import com.epam.agency.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vouchers")
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping
    public ModelAndView showVouchersPage(@RequestParam(required = false) String tourType,
                                         @RequestParam(required = false) String transferType,
                                         @RequestParam(required = false) BigDecimal minPrice,
                                         @RequestParam(required = false) BigDecimal maxPrice,
                                         @RequestParam(required = false) String hotelType,
                                         @RequestParam(required = false) Boolean hotStatus,
                                         @PageableDefault(page = 0, size = 3, sort = "hotStatus", direction = Sort.Direction.DESC) Pageable pageable,
                                         ModelMap model) {

        Page<VoucherDTO> vouchers = voucherService.getAllAvailableVouchers(tourType, transferType, minPrice, maxPrice,
                hotelType, hotStatus, pageable);

        model.addAttribute("tourTypes", TourType.values());
        model.addAttribute("transferTypes", TransferType.values());
        model.addAttribute("hotelTypes", HotelType.values());

        model.addAttribute("voucherPage", vouchers);
        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("totalPages", vouchers.getTotalPages());

        model.addAttribute("tourType", tourType);
        model.addAttribute("transferType", transferType);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("hotelType", hotelType);
        model.addAttribute("hotStatus", hotStatus);

        return new ModelAndView("vouchers-page", model);
    }

    @GetMapping("/info/{voucherId}")
    public ModelAndView showVoucherInfoPage(@PathVariable("voucherId") String voucherId, ModelMap model) {
        VoucherDTO voucherDTO = voucherService.getVoucherDTOById(voucherId);
        model.addAttribute("voucher", voucherDTO);
        return new ModelAndView("voucher-info-page", model);
    }
}
