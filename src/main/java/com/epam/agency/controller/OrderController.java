package com.epam.agency.controller;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.dto.UserDTO;
import com.epam.agency.dto.VoucherDTO;
import com.epam.agency.exception.AlreadyExistsException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.exception.NotFoundException;
import com.epam.agency.service.FileService;
import com.epam.agency.service.OrderService;
import com.epam.agency.service.UserService;
import com.epam.agency.service.VoucherService;
import com.epam.agency.util.MessageUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final VoucherService voucherService;
    private final UserService userService;
    private final MessageUtil messageUtil;
    private final FileService fileService;

    @Value("${stripe.key.public}")
    private String stripePublicKey;

    @GetMapping("/create/{voucherId}")
    public ModelAndView showOrderPage(@PathVariable("voucherId") String voucherId, ModelMap model) {
        VoucherDTO voucher = voucherService.getVoucherDTOById(voucherId);

        model.addAttribute("order", new OrderDTO());
        model.addAttribute("voucher", voucher);

        return new ModelAndView("order-create-page", model);
    }

    @PostMapping("/create/{voucherId}")
    public ModelAndView createOrder(@PathVariable("voucherId") String voucherId,
                                    @ModelAttribute("order") OrderDTO order,
                                    RedirectAttributes redirectAttributes,
                                    ModelMap model) {
        VoucherDTO voucher = voucherService.getVoucherDTOById(voucherId);
        UserDTO user = userService.getUserDTO();
        order.setVoucherId(voucherId);
        order.setUserId(user.getId());

        try {
            orderService.createOrder(order);
            redirectAttributes.addFlashAttribute("message", "orderRegistered");
            return new ModelAndView("redirect:/users/info");
        } catch (AlreadyExistsException | NotFoundException | InvalidDataException exception) {
            model.addAttribute("voucher", voucher);
            model.addAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("order-create-page", model);
        }
    }

    @GetMapping("/{orderId}/pdf")
    public void generateOrderPdf(@PathVariable String orderId, HttpServletResponse response) throws Exception {
        fileService.generateOrderPdf(orderId, response);
    }

    @GetMapping("/pay/{orderId}")
    public String showPaymentPage(@PathVariable String orderId, ModelMap model) {
        OrderDTO order = orderService.getOrderById(orderId);

        model.addAttribute("order", order);
        model.addAttribute("amount", order.getOrderPrice());
        model.addAttribute("stripePublicKey", stripePublicKey);
        return "payment-page";
    }

    @PostMapping("/pay/{orderId}")
    public ModelAndView payOrder(@PathVariable("orderId") String orderId,
                                 @RequestParam("stripeToken") String stripeToken,
                                 RedirectAttributes redirectAttributes) {
        try {
            orderService.payOrder(orderId, stripeToken);
            redirectAttributes.addFlashAttribute("message", "orderPaid");
            return new ModelAndView("redirect:/users/info");
        } catch (NotFoundException | InvalidDataException exception) {
            redirectAttributes.addFlashAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("redirect:/order/pay/" + orderId);
        }
    }

    @PostMapping("/delete/{orderId}")
    public ModelAndView deleteOrder(@PathVariable("orderId") String orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(orderId);
            redirectAttributes.addFlashAttribute("message", "orderDeleted");
            return new ModelAndView("redirect:/users/info");
        } catch (InvalidDataException exception) {
            redirectAttributes.addFlashAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("redirect:/users/info");
        }
    }
}
