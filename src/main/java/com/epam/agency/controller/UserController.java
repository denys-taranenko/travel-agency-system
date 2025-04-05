package com.epam.agency.controller;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.dto.UserDTO;
import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.dto.group.OnUpdate;
import com.epam.agency.exception.AlreadyExistsException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.payment.StripeService;
import com.epam.agency.service.FileService;
import com.epam.agency.service.OrderService;
import com.epam.agency.service.UserService;
import com.epam.agency.service.VoucherService;
import com.epam.agency.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final OrderService orderService;
    private final VoucherService voucherService;
    private final FileService fileService;
    private final MessageUtil messageUtil;
    private final StripeService stripeService;

    @Value("${stripe.key.public}")
    private String stripePublicKey;

    @GetMapping
    public ModelAndView showUsersPage(@RequestParam(required = false) Boolean accountStatus,
                                      @RequestParam(defaultValue = "accountStatus") String sortField,
                                      @RequestParam(defaultValue = "desc") String sortDirection,
                                      @RequestParam(value = "search", required = false) String search,
                                      @PageableDefault(page = 0, size = 7, sort = "accountStatus", direction = Sort.Direction.ASC) Pageable pageable,
                                      ModelMap model) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<UserDTO> allUsers = userService.getAllUsers(accountStatus, search, pageable);

        model.addAttribute("userPage", allUsers);

        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("totalPages", allUsers.getTotalPages());
        model.addAttribute("accountStatus", accountStatus);

        model.addAttribute("search", search);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);

        return new ModelAndView("user-panel-page", model);
    }

    @GetMapping("/status/{userId}")
    public String changeAccountStatus(@PathVariable("userId") String userId,
                                      RedirectAttributes redirectAttributes) {
        userService.changeUserStatus(userId);
        redirectAttributes.addFlashAttribute("message", "changeStatus");
        return "redirect:/users";
    }

    @GetMapping("/info")
    public ModelAndView showUserInfoPage(ModelMap model) {
        UserDTO user = userService.getUserDTO();
        List<OrderDTO> orders = orderService.getAllUserOrders(user.getId());
        List<VoucherDTO> vouchers = voucherService.getUserVouchers(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("vouchers", vouchers);

        return new ModelAndView("user-info-page", model);
    }

    @GetMapping("/info/{userId}")
    public ModelAndView showUserInfoPageForManagement(@PathVariable("userId") String userId, ModelMap model) {
        UserDTO user = userService.getUserDTOById(userId);

        List<OrderDTO> orders = orderService.getAllUserOrders(user.getId());
        List<VoucherDTO> vouchers = voucherService.getUserVouchers(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("vouchers", vouchers);
        return new ModelAndView("user-info-page", model);
    }

    @PostMapping("/info/cancel/{orderId}")
    public ModelAndView cancelOrder(@PathVariable("orderId") String orderId,
                                    @RequestParam(required = false) String reason,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(orderId, reason);
            redirectAttributes.addFlashAttribute("message", "orderCanceled");
            return new ModelAndView("redirect:/users/info");
        } catch (InvalidDataException exception) {
            redirectAttributes.addFlashAttribute("error", messageUtil.getMessage(exception.getMessage()));
            return new ModelAndView("redirect:/users/info");
        }
    }

    @GetMapping("/top-up")
    public String showTopUpPage(ModelMap model) {
        model.addAttribute("stripePublicKey", stripePublicKey);
        return "top-up-balance-page";
    }

    @PostMapping("/top-up")
    public ModelAndView topUpBalance(
            @RequestParam("stripeToken") String stripeToken,
            @RequestParam("amount") BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {
            UserDTO user = userService.getUserDTO();
            stripeService.topUpBalance(stripeToken, amount, "usd", UUID.fromString(user.getId()));

            redirectAttributes.addFlashAttribute("message", "changeBalance");
            return new ModelAndView("redirect:/users/info");
        } catch (InvalidDataException exception) {
            redirectAttributes.addFlashAttribute("error", messageUtil.getMessage(exception.getMessage()));
            return new ModelAndView("redirect:/users/top-up");
        }
    }

    @GetMapping("/info/settings")
    public ModelAndView showSettingsPage(ModelMap model) {
        UserDTO user = userService.getUserDTO();
        String[] avatars = fileService.getAvailableAvatars();

        model.addAttribute("user", user);
        model.addAttribute("avatars", avatars);
        return new ModelAndView("user-settings-page", model);
    }

    @PostMapping("/info/settings")
    public ModelAndView updateUser(@ModelAttribute("user") @Validated(OnUpdate.class) UserDTO userDTO,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   ModelMap model) {
        UserDTO user = userService.getUserDTO();
        String[] avatars = fileService.getAvailableAvatars();

        if (bindingResult.hasErrors()) {
            model.addAttribute("avatars", avatars);
            model.addAttribute("user", userDTO);
            return new ModelAndView("user-settings-page", model);
        }

        try {
            userService.updateUser(userDTO);
            redirectAttributes.addFlashAttribute("message", "changeSettings");
            return new ModelAndView("redirect:/users/info/settings");
        } catch (AlreadyExistsException | InvalidDataException exception) {
            model.addAttribute("user", user);
            model.addAttribute("avatars", avatars);
            model.addAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("user-settings-page", model);
        }
    }

    @PostMapping("/info/settings/avatar")
    public ModelAndView changeAvatar(@RequestParam("avatar") String avatar, ModelMap model) {
        UserDTO user = userService.getUserDTO();
        userService.updateUserAvatar(avatar, user.getId());

        model.addAttribute("user", user);
        return new ModelAndView("redirect:/users/info/settings", model);
    }
}
