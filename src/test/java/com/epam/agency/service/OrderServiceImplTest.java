package com.epam.agency.service;

import com.epam.finaltask.dto.OrderDTO;
import com.epam.finaltask.exception.AlreadyExistsException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.exception.status.StatusCodes;
import com.epam.finaltask.exception.status.StatusMessages;
import com.epam.finaltask.mapper.OrderMapper;
import com.epam.finaltask.model.Order;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_SuccessTest() {
        // Given
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(UUID.randomUUID().toString());
        orderDTO.setVoucherId(UUID.randomUUID().toString());

        Order order = new Order();
        order.setUser(new User());
        order.setVoucher(new Voucher());

        User user = new User();
        Voucher voucher = new Voucher();

        when(orderRepository.existsByUserIdAndVoucherId(any(), any())).thenReturn(false);
        when(orderMapper.toOrder(orderDTO)).thenReturn(order);
        when(userService.getUserById(orderDTO.getUserId())).thenReturn(user);
        when(voucherService.getVoucherById(orderDTO.getVoucherId())).thenReturn(voucher);

        // When
        orderService.createOrder(orderDTO);

        // Then
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void createOrder_OrderExists_ThrowsAlreadyExistsException() {
        // Given
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(UUID.randomUUID().toString());
        orderDTO.setVoucherId(UUID.randomUUID().toString());

        when(orderRepository.existsByUserIdAndVoucherId(any(), any())).thenReturn(true);

        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> {
            orderService.createOrder(orderDTO);
        });

        assertEquals(StatusCodes.CONFLICT.name(), exception.getStatusCode());
        assertEquals(StatusMessages.VOUCHER_ALREADY_HAVE.getStatusMessage(), exception.getMessage());
    }

    @Test
    void testGetOrderById() {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order();
        order.setId(UUID.fromString(orderId));

        when(orderRepository.findById(UUID.fromString(orderId))).thenReturn(Optional.of(order));

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(orderId);

        when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(orderDTO);
        OrderDTO result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
    }

    @Test
    void testGetOrderByIdNotFound() {
        String orderId = UUID.randomUUID().toString();

        when(orderRepository.findById(UUID.fromString(orderId))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    void testCancelOrderNotFound() {
        String orderId = UUID.randomUUID().toString();

        when(orderRepository.findById(UUID.fromString(orderId))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.cancelOrder(orderId, "Cancellation reason"));
    }
}
