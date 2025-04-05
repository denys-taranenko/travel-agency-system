package com.epam.agency.service;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.exception.AlreadyExistsException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.exception.NotFoundException;
import com.epam.agency.exception.status.StatusCodes;
import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.mapper.OrderMapper;
import com.epam.agency.model.Order;
import com.epam.agency.model.User;
import com.epam.agency.model.Voucher;
import com.epam.agency.model.enums.OrderStatus;
import com.epam.agency.payment.StripeService;
import com.epam.agency.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final VoucherService voucherService;
    private final StripeService stripeService;

    @Override
    public void createOrder(OrderDTO orderDTO) {
        if (orderRepository.existsByUserIdAndVoucherId(UUID.fromString(orderDTO.getUserId()), UUID.fromString(orderDTO.getVoucherId()))) {
            throw new AlreadyExistsException(StatusCodes.CONFLICT.name(), StatusMessages.VOUCHER_ALREADY_HAVE.getStatusMessage());
        }

        User user = userService.getUserById(orderDTO.getUserId());
        Voucher voucher = voucherService.getVoucherById(orderDTO.getVoucherId());

        Order order = Order.builder()
                .user(user)
                .voucher(voucher)
                .orderPrice(voucher.getPrice())
                .voucherArrivalDate(voucher.getArrivalDate())
                .voucherEvictionDate(voucher.getEvictionDate())
                .orderDate(LocalDate.now())
                .status(OrderStatus.REGISTERED)
                .message(orderDTO.getMessage() != null && orderDTO.getMessage().isEmpty() ? null : orderDTO.getMessage())
                .cancelReason(null)
                .callBackRequest(orderDTO.isCallBackRequest())
                .lastCustomerId(user.getId())
                .build();

        orderRepository.save(order);
    }

    @Override
    public void approveRequest(String orderId) {
        OrderDTO order = getOrderById(orderId);
        order.setStatus(OrderStatus.APPROVED.name());
        orderRepository.save(orderMapper.toOrder(order));
    }

    @Override
    public void cancelRequest(String orderId) {
        OrderDTO orderDTO = getOrderById(orderId);
        Order order = orderMapper.toOrder(orderDTO);
        User user = userService.getUserById(orderDTO.getUserId());

        if (order.getStatus().equals(OrderStatus.PAID_AND_CANCELED)) {
            BigDecimal orderPrice = order.getOrderPrice();
            user.setBalance(user.getBalance().add(orderPrice));
        }

        order.setStatus(OrderStatus.PROCESSED);
        order.setLastCustomerId(user.getId());

        orderRepository.saveAndFlush(order);
        userService.saveUser(user);
    }

    @Override
    public OrderDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId)).orElseThrow(() ->
                new NotFoundException(StatusCodes.NOT_FOUND.name(), StatusMessages.ORDER_NOT_FOUND.getStatusMessage()));
        return orderMapper.toOrderDTO(order);
    }

    @Override
    public Page<OrderDTO> getAllOrders(String status, Pageable pageable) {
        OrderStatus parsedStatus = parseEnum(status, OrderStatus.class);

        Page<Order> orders = orderRepository.findAllOrders(parsedStatus, pageable);
        return orders.map(orderMapper::toOrderDTO);
    }

    @Override
    public void cancelOrder(String orderId, String reason) {
        OrderDTO orderDTO = getOrderById(orderId);
        Order order = orderMapper.toOrder(orderDTO);

        if (order.getVoucherArrivalDate().plusDays(2).isBefore(LocalDate.now())) {
            throw new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.BAD_CANCELING_ORDER.getStatusMessage());
        }

        if (reason != null && !reason.trim().isEmpty()) {
            order.setCancelReason(reason);
        }

        if (order.getStatus().equals(OrderStatus.PAID)) {
            order.setStatus(OrderStatus.PAID_AND_CANCELED);
        } else {
            order.setStatus(OrderStatus.CANCELED);
        }

        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void payOrder(String orderId, String stripeToken) {
        OrderDTO orderDTO = getOrderById(orderId);
        Order order = orderMapper.toOrder(orderDTO);
        Voucher voucher = voucherService.getVoucherById(orderDTO.getVoucherId());
        User user = userService.getUserById(orderDTO.getUserId());

        if (user.getBalance().compareTo(voucher.getPrice()) < 0) {
            throw new InvalidDataException(StatusCodes.NOT_FOUND.name(), StatusMessages.NOT_ENOUGH_FUNDS.getStatusMessage());
        }

        stripeService.charge(
                stripeToken,
                voucher.getPrice(),
                "usd",
                "Payment order " + orderId
        );

        order.setStatus(OrderStatus.PAID);
        user.setBalance(user.getBalance().subtract(voucher.getPrice()));

        userService.saveUser(user);
        orderRepository.save(order);
    }

    @Override
    public void deleteOrder(String orderId) {
        OrderDTO orderDTO = getOrderById(orderId);
        Order order = orderMapper.toOrder(orderDTO);

        if (order.getStatus().equals(OrderStatus.PAID) && order.getVoucherEvictionDate().isBefore(LocalDate.now()) || order.getStatus().equals(OrderStatus.PROCESSED)) {
            order.setStatus(OrderStatus.DELETED);
            order.setUser(null);
            orderRepository.save(order);
        } else {
            throw new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.BAD_DELETING_ORDER.getStatusMessage());
        }
        orderRepository.save(order);
    }

    @Override
    public List<OrderDTO> getAllUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(UUID.fromString(userId));
        return orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.DELETED)
                .map(orderMapper::toOrderDTO)
                .toList();
    }

    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumClass) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
