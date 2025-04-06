package com.epam.agency.service;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.mapper.OrderMapper;
import com.epam.agency.model.Order;
import com.epam.agency.model.User;
import com.epam.agency.model.Voucher;
import com.epam.agency.model.enums.OrderStatus;
import com.epam.agency.payment.StripeService;
import com.epam.agency.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private VoucherService voucherService;

    @Mock
    private StripeService stripeService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_ShouldSuccessfullyCreateOrder() {
        //Arrange
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String voucherId = "550e8400-e29b-41d4-a716-446655440001";

        OrderDTO orderDTO = OrderDTO.builder()
                .userId(userId)
                .voucherId(voucherId)
                .message("Test message")
                .callBackRequest(true)
                .build();

        User mockUser = new User();
        mockUser.setId(UUID.fromString(userId));

        Voucher mockVoucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .price(BigDecimal.valueOf(100))
                .arrivalDate(LocalDate.now().plusDays(1))
                .evictionDate(LocalDate.now().plusDays(3))
                .build();

        when(orderRepository.existsByUserIdAndVoucherId(any(), any())).thenReturn(false);
        when(userService.getUserById(any())).thenReturn(mockUser);
        when(voucherService.getVoucherById(any())).thenReturn(mockVoucher);

        //Act
        orderService.createOrder(orderDTO);

        //Assert
        verify(orderRepository, times(1)).existsByUserIdAndVoucherId(any(), any());
        verify(userService, times(1)).getUserById(any());
        verify(voucherService, times(1)).getVoucherById(any());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void approveRequest_ShouldChangeStatusToApproved_WhenOrderExists() {
        // Arrange
        String orderId = "550e8400-e29b-41d4-a716-446655440000";
        UUID orderUuid = UUID.fromString(orderId);

        Order existingOrder = Order.builder()
                .id(orderUuid)
                .status(OrderStatus.REGISTERED)
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderId)
                .status(OrderStatus.REGISTERED.name())
                .build();

        Order expectedOrder = Order.builder()
                .id(orderUuid)
                .status(OrderStatus.APPROVED)
                .build();

        when(orderRepository.findById(orderUuid)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toOrderDTO(existingOrder)).thenReturn(orderDTO);
        when(orderMapper.toOrder(any(OrderDTO.class))).thenReturn(expectedOrder);

        // Act
        orderService.approveRequest(orderId);

        // Assert
        verify(orderRepository).save(argThat(order -> order.getId().equals(orderUuid) &&
                order.getStatus() == OrderStatus.APPROVED));

        verify(orderRepository).findById(orderUuid);
        verify(orderMapper).toOrderDTO(existingOrder);
        verify(orderMapper).toOrder(any(OrderDTO.class));
    }

    @Test
    void cancelRequest_ShouldProcessPaidOrderAndRefund_WhenOrderIsPaid() {
        // Arrange
        String orderId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "550e8400-e29b-41d4-a716-446655440001";

        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        BigDecimal orderPrice = BigDecimal.valueOf(200);

        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.PAID_AND_CANCELED.name())
                .build();

        Order order = Order.builder()
                .id(UUID.fromString(orderId))
                .status(OrderStatus.PAID_AND_CANCELED)
                .orderPrice(orderPrice)
                .build();

        User user = User.builder()
                .id(UUID.fromString(userId))
                .balance(initialBalance)
                .build();

        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(orderDTO);
        when(orderMapper.toOrder(any(OrderDTO.class))).thenReturn(order);
        when(userService.getUserById(any(String.class))).thenReturn(user);

        // Act
        orderService.cancelRequest(orderId);

        // Assert
        verify(orderRepository).saveAndFlush(argThat(o -> o.getStatus() == OrderStatus.PROCESSED &&
                o.getLastCustomerId().equals(user.getId())));

        assertThat(user.getBalance()).isEqualTo(initialBalance.add(orderPrice));
        verify(userService).saveUser(user);

        verify(orderRepository).findById(UUID.fromString(orderId));
        verify(userService).getUserById(userId);
    }

    @Test
    void getOrderById_ShouldReturnOrderDTO_WhenOrderExists() {
        // Arrange
        String orderId = "550e8400-e29b-41d4-a716-446655440000";
        UUID orderUuid = UUID.fromString(orderId);
        BigDecimal orderPrice = BigDecimal.valueOf(1500);

        Order mockOrder = Order.builder()
                .id(orderUuid)
                .status(OrderStatus.REGISTERED)
                .orderPrice(orderPrice)
                .build();

        OrderDTO expectedOrderDTO = OrderDTO.builder()
                .id(orderId)
                .status(OrderStatus.REGISTERED.name())
                .orderPrice(orderPrice)
                .build();

        when(orderRepository.findById(orderUuid)).thenReturn(Optional.of(mockOrder));
        when(orderMapper.toOrderDTO(mockOrder)).thenReturn(expectedOrderDTO);

        // Act
        OrderDTO result = orderService.getOrderById(orderId);

        // Assert
        assertThat(result).isNotNull()
                .extracting(
                        OrderDTO::getId,
                        OrderDTO::getStatus,
                        OrderDTO::getOrderPrice)
                .containsExactly(
                        orderId,
                        OrderStatus.REGISTERED.name(),
                        orderPrice);

        verify(orderRepository).findById(orderUuid);
        verify(orderMapper).toOrderDTO(mockOrder);
    }

    @Test
    void getAllOrders_ShouldReturnPageOfOrderDTOs_WithStatusFilter() {
        // Arrange
        String status = "REGISTERED";
        Pageable pageable = PageRequest.of(0, 10);

        Order order1 = Order.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.REGISTERED)
                .build();

        Order order2 = Order.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.REGISTERED)
                .build();

        Page<Order> mockPage = new PageImpl<>(List.of(order1, order2), pageable, 2);

        OrderDTO dto1 = OrderDTO.builder()
                .id(order1.getId().toString())
                .status(status)
                .build();

        OrderDTO dto2 = OrderDTO.builder()
                .id(order2.getId().toString())
                .status(status)
                .build();

        when(orderRepository.findAllOrders(OrderStatus.REGISTERED, pageable))
                .thenReturn(mockPage);
        when(orderMapper.toOrderDTO(order1)).thenReturn(dto1);
        when(orderMapper.toOrderDTO(order2)).thenReturn(dto2);

        // Act
        Page<OrderDTO> result = orderService.getAllOrders(status, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        verify(orderRepository).findAllOrders(OrderStatus.REGISTERED, pageable);
        verify(orderMapper, times(2)).toOrderDTO(any(Order.class));
    }

    @Test
    void cancelOrder_ShouldCancelPaidOrderAndSetReason_WhenWithinAllowedPeriod() {
        // Arrange
        String orderId = "550e8400-e29b-41d4-a716-446655440000";
        String reason = "Test cancellation reason";
        LocalDate arrivalDate = LocalDate.now().plusDays(1);

        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderId)
                .status(OrderStatus.PAID.name())
                .voucherArrivalDate(arrivalDate)
                .build();

        Order order = Order.builder()
                .id(UUID.fromString(orderId))
                .status(OrderStatus.PAID)
                .voucherArrivalDate(arrivalDate)
                .build();

        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(orderDTO);
        when(orderMapper.toOrder(any(OrderDTO.class))).thenReturn(order);

        // Act
        orderService.cancelOrder(orderId, reason);

        // Assert
        verify(orderRepository).save(argThat(savedOrder -> savedOrder.getStatus() == OrderStatus.PAID_AND_CANCELED &&
                savedOrder.getCancelReason().equals(reason)));

        verify(orderRepository).findById(UUID.fromString(orderId));
        verify(orderMapper).toOrderDTO(order);
        verify(orderMapper).toOrder(orderDTO);
    }

    @Test
    void payOrder_ShouldProcessPayment_WhenSufficientFunds() {
        // Arrange
        String orderId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "550e8400-e29b-41d4-a716-446655440001";
        String voucherId = "550e8400-e29b-41d4-a716-446655440002";
        String stripeToken = "test_stripe_token";

        BigDecimal initialBalance = BigDecimal.valueOf(2000);
        BigDecimal voucherPrice = BigDecimal.valueOf(1500);
        BigDecimal expectedBalance = initialBalance.subtract(voucherPrice);

        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderId)
                .userId(userId)
                .voucherId(voucherId)
                .status(OrderStatus.REGISTERED.name())
                .build();

        Order order = Order.builder()
                .id(UUID.fromString(orderId))
                .status(OrderStatus.REGISTERED)
                .build();

        Voucher voucher = Voucher.builder()
                .id(UUID.fromString(voucherId))
                .price(voucherPrice)
                .build();

        User user = User.builder()
                .id(UUID.fromString(userId))
                .balance(initialBalance)
                .build();

        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(orderDTO);
        when(orderMapper.toOrder(any(OrderDTO.class))).thenReturn(order);
        when(voucherService.getVoucherById(voucherId)).thenReturn(voucher);
        when(userService.getUserById(userId)).thenReturn(user);

        // Act
        orderService.payOrder(orderId, stripeToken);

        // Assert
        verify(stripeService).charge(
                eq(stripeToken),
                eq(voucherPrice),
                eq("usd"),
                eq("Payment order " + orderId));

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.PAID));
        verify(userService).saveUser(argThat(u -> u.getBalance().compareTo(expectedBalance) == 0));
        verify(orderRepository).findById(UUID.fromString(orderId));
        verify(voucherService).getVoucherById(voucherId);
        verify(userService).getUserById(userId);
    }

    @Test
    void deleteOrder_ShouldCallSaveTwice_WhenPaidAndAfterEvictionDate() {
        // Arrange
        String orderId = "550e8400-e29b-41d4-a716-446655440000";
        LocalDate pastEvictionDate = LocalDate.now().minusDays(1);

        Order order = Order.builder()
                .id(UUID.fromString(orderId))
                .status(OrderStatus.PAID)
                .voucherEvictionDate(pastEvictionDate)
                .user(new User())
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderId)
                .status(OrderStatus.PAID.name())
                .voucherEvictionDate(pastEvictionDate)
                .build();

        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(orderDTO);
        when(orderMapper.toOrder(any(OrderDTO.class))).thenReturn(order);

        // Act
        orderService.deleteOrder(orderId);

        // Assert
        verify(orderRepository, times(2)).save(argThat(o -> o.getStatus() == OrderStatus.DELETED &&
                o.getUser() == null));
    }

    @Test
    void deleteOrder_ShouldCallSaveTwice_WhenStatusIsProcessed() {
        // Arrange
        String orderId = "550e8400-e29b-41d4-a716-446655440000";

        Order order = Order.builder()
                .id(UUID.fromString(orderId))
                .status(OrderStatus.PROCESSED)
                .user(new User())
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderId)
                .status(OrderStatus.PROCESSED.name())
                .build();

        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(orderDTO);
        when(orderMapper.toOrder(any(OrderDTO.class))).thenReturn(order);

        // Act
        orderService.deleteOrder(orderId);

        // Assert
        verify(orderRepository, times(2)).save(argThat(o -> o.getStatus() == OrderStatus.DELETED));
    }

    @Test
    void getAllUserOrders_ShouldReturnNonDeletedOrders_ForExistingUser() {
        // Arrange
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UUID userUuid = UUID.fromString(userId);

        Order activeOrder1 = Order.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.REGISTERED)
                .user(User.builder().id(userUuid).build())
                .build();

        Order activeOrder2 = Order.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.PAID)
                .user(User.builder().id(userUuid).build())
                .build();

        Order deletedOrder = Order.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.DELETED)
                .user(User.builder().id(userUuid).build())
                .build();

        List<Order> allOrders = List.of(activeOrder1, activeOrder2, deletedOrder);

        OrderDTO dto1 = OrderDTO.builder()
                .id(activeOrder1.getId().toString())
                .status(activeOrder1.getStatus().name())
                .build();

        OrderDTO dto2 = OrderDTO.builder()
                .id(activeOrder2.getId().toString())
                .status(activeOrder2.getStatus().name())
                .build();

        when(orderRepository.findByUserId(userUuid)).thenReturn(allOrders);
        when(orderMapper.toOrderDTO(activeOrder1)).thenReturn(dto1);
        when(orderMapper.toOrderDTO(activeOrder2)).thenReturn(dto2);

        // Act
        List<OrderDTO> result = orderService.getAllUserOrders(userId);

        // Assert
        assertThat(result).hasSize(2).extracting(OrderDTO::getId).containsExactlyInAnyOrder(
                activeOrder1.getId().toString(),
                activeOrder2.getId().toString());

        verify(orderMapper, never()).toOrderDTO(deletedOrder);
        verify(orderRepository).findByUserId(userUuid);
    }
}
