package com.epam.agency.controller;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.dto.group.OnChange;
import com.epam.agency.dto.group.OnCreate;
import com.epam.agency.dto.group.OnUpdate;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.model.enums.*;
import com.epam.agency.service.OrderService;
import com.epam.agency.service.VoucherService;
import com.epam.agency.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management")
public class ManagementController {
    private final MessageUtil messageUtil;
    private final VoucherService voucherService;
    private final OrderService orderService;

    @GetMapping
    public String showAdminPanel() {
        return "management-panel-page";
    }

    @GetMapping("/vouchers")
    public ModelAndView showVouchers(@RequestParam(required = false) String tourType,
                                     @RequestParam(required = false) String transferType,
                                     @RequestParam(required = false) String hotelType,
                                     @RequestParam(required = false) Boolean hotStatus,
                                     @RequestParam(required = false) String search,
                                     @RequestParam(defaultValue = "hotStatus") String sortField,
                                     @RequestParam(defaultValue = "desc") String sortDirection,
                                     @PageableDefault(page = 0, size = 7, sort = "hotStatus", direction = Sort.Direction.ASC) Pageable pageable,
                                     ModelMap model) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<VoucherDTO> vouchers = voucherService.getAllVouchersForManagement(tourType, transferType, hotelType, hotStatus, search, pageable);

        model.addAttribute("tourTypes", TourType.values());
        model.addAttribute("transferTypes", TransferType.values());
        model.addAttribute("hotelTypes", HotelType.values());

        model.addAttribute("tourType", tourType);
        model.addAttribute("transferType", transferType);
        model.addAttribute("hotelType", hotelType);
        model.addAttribute("hotStatus", hotStatus);

        model.addAttribute("voucherPage", vouchers);

        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("totalPages", vouchers.getTotalPages());
        model.addAttribute("totalItems", voucherService.getVoucherCount());
        model.addAttribute("search", search);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);

        return new ModelAndView("management-panel-vouchers-page", model);
    }

    @GetMapping("/archive")
    public ModelAndView showArchive(@RequestParam(required = false) String tourType,
                                    @RequestParam(required = false) String transferType,
                                    @RequestParam(required = false) String hotelType,
                                    @RequestParam(required = false) String search,
                                    @RequestParam(defaultValue = "status") String sortField,
                                    @RequestParam(defaultValue = "desc") String sortDirection,
                                    @PageableDefault(page = 0, size = 7, direction = Sort.Direction.ASC) Pageable pageable,
                                    ModelMap model) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<VoucherDTO> vouchers = voucherService.getAllArchiveVouchers(tourType, transferType, hotelType, search, pageable);

        model.addAttribute("tourTypes", TourType.values());
        model.addAttribute("transferTypes", TransferType.values());
        model.addAttribute("hotelTypes", HotelType.values());

        model.addAttribute("tourType", tourType);
        model.addAttribute("transferType", transferType);
        model.addAttribute("hotelType", hotelType);

        model.addAttribute("voucherPage", vouchers);

        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("totalPages", vouchers.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);

        return new ModelAndView("management-panel-archive-vouchers-page", model);
    }

    @GetMapping("/create")
    public ModelAndView showCreateVoucherPage(ModelMap model) {
        VoucherDTO voucherDTO = new VoucherDTO();

        model.addAttribute("voucher", voucherDTO);
        model.addAttribute("tourTypes", TourType.values());
        model.addAttribute("transferTypes", TransferType.values());
        model.addAttribute("hotelTypes", HotelType.values());
        return new ModelAndView("create-voucher-page", model);
    }

    @PostMapping("/create")
    public ModelAndView saveVoucher(@ModelAttribute("voucher") @Validated(OnCreate.class) VoucherDTO voucherDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    ModelMap model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tourTypes", TourType.values());
            model.addAttribute("transferTypes", TransferType.values());
            model.addAttribute("hotelTypes", HotelType.values());
            return new ModelAndView("create-voucher-page", model);
        }

        try {
            VoucherDTO savedVoucher = voucherService.createVoucher(voucherDTO);
            redirectAttributes.addFlashAttribute("voucherId", savedVoucher.getId());
            redirectAttributes.addFlashAttribute("message", "created");
            return new ModelAndView("redirect:/management");
        } catch (InvalidDataException exception) {
            model.addAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("create-voucher-page", model);
        }
    }

    @GetMapping("/edit/{voucherId}")
    public ModelAndView showEditVoucherPage(@PathVariable("voucherId") String voucherId, ModelMap model) {
        VoucherDTO voucherDTO = voucherService.getVoucherDTOById(voucherId);

        model.addAttribute("voucher", voucherDTO);
        model.addAttribute("tourTypes", TourType.values());
        model.addAttribute("transferTypes", TransferType.values());
        model.addAttribute("hotelTypes", HotelType.values());
        return new ModelAndView("edit-voucher-page", model);
    }

    @PostMapping("/edit/{voucherId}")
    public ModelAndView editVoucher(@ModelAttribute("voucher") @Validated(OnUpdate.class) VoucherDTO voucherDTO,
                                    BindingResult bindingResult,
                                    @PathVariable("voucherId") String voucherId,
                                    RedirectAttributes redirectAttributes,
                                    ModelMap model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tourTypes", TourType.values());
            model.addAttribute("transferTypes", TransferType.values());
            model.addAttribute("hotelTypes", HotelType.values());
            return new ModelAndView("edit-voucher-page", model);
        }

        try {
            VoucherDTO updatedVoucher = voucherService.updateVoucher(voucherId, voucherDTO);
            model.addAttribute("voucherId", updatedVoucher.getId());
            redirectAttributes.addFlashAttribute("message", "updated");
            return new ModelAndView("redirect:/vouchers/info/" + updatedVoucher.getId());
        } catch (InvalidDataException exception) {
            model.addAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("edit-voucher-page", model);
        }
    }

    @GetMapping("/delete/{voucherId}")
    public String deleteVoucher(@PathVariable("voucherId") String voucherId,
                                RedirectAttributes redirectAttributes) {
        voucherService.deleteVoucher(voucherId);
        redirectAttributes.addFlashAttribute("message", "deleted");
        return "redirect:/management/vouchers";
    }

    @GetMapping("/restore/{voucherId}")
    public String restoreVoucher(@PathVariable("voucherId") String voucherId,
                                RedirectAttributes redirectAttributes) {
        voucherService.restoreVoucher(voucherId);
        redirectAttributes.addFlashAttribute("message", "restore");
        return "redirect:/management/vouchers";
    }

    @GetMapping("/requests")
    public ModelAndView showRequestsPage(@RequestParam(required = false) String status,
                                         @RequestParam(defaultValue = "status") String sortField,
                                         @RequestParam(defaultValue = "desc") String sortDirection,
                                         @PageableDefault(page = 0, size = 7, direction = Sort.Direction.ASC) Pageable pageable,
                                         ModelMap model) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<OrderDTO> orders = orderService.getAllOrders(status, pageable);

        model.addAttribute("orderStatuses", Arrays.asList(OrderStatus.REGISTERED, OrderStatus.CANCELED, OrderStatus.PAID_AND_CANCELED));
        model.addAttribute("status", status);

        model.addAttribute("orderPage", orders);
        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);

        return new ModelAndView("order-requests-page", model);
    }

    @GetMapping("/change/{voucherId}")
    public ModelAndView showChangeVoucherPage(@PathVariable("voucherId") String voucherId, ModelMap model) {
        VoucherDTO voucherDTO = voucherService.getVoucherDTOById(voucherId);

        model.addAttribute("voucher", voucherDTO);
        model.addAttribute("voucherStatuses", Arrays.stream(VoucherStatus.values())
                .filter(status -> !status.equals(VoucherStatus.DELETED))
                .toArray());
        return new ModelAndView("change-voucher-page", model);
    }

    @PostMapping("/change/{voucherId}")
    public ModelAndView changeVoucherStatus(@ModelAttribute("voucher") @Validated(OnChange.class) VoucherDTO voucherDTO,
                                            BindingResult bindingResult,
                                            @PathVariable("voucherId") String voucherId,
                                            RedirectAttributes redirectAttributes,
                                            ModelMap model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("voucherStatuses", VoucherStatus.values());
            model.addAttribute("voucher", voucherDTO);
            return new ModelAndView("change-voucher-page", model);
        }

        try {
            VoucherDTO changedVoucher = voucherService.changeVoucherStatus(voucherId, voucherDTO);
            model.addAttribute("voucherId", changedVoucher.getId());
            redirectAttributes.addFlashAttribute("message", "changed");
            return new ModelAndView("redirect:/vouchers/info/" + changedVoucher.getId());
        } catch (InvalidDataException exception) {
            model.addAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("change-voucher-page", model);
        }
    }

    @GetMapping("/order/{orderId}/approve")
    public String approveRequest(@PathVariable("orderId") String orderId,
                                 RedirectAttributes redirectAttributes) {
        orderService.approveRequest(orderId);
        redirectAttributes.addFlashAttribute("message", "approve");
        return "redirect:/management/requests";
    }

    @GetMapping("/order/{orderId}/cancel")
    public String cancelRequest(@PathVariable("orderId") String orderId,
                                RedirectAttributes redirectAttributes) {
        orderService.cancelRequest(orderId);
        redirectAttributes.addFlashAttribute("message", "cancel");
        return "redirect:/management/requests";
    }
}
