package com.epam.agency.mapper;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.model.Order;
import com.epam.agency.model.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;

@Mapper(componentModel = "spring", imports = {UUID.class, OrderStatus.class},
        injectionStrategy = CONSTRUCTOR)
public interface OrderMapper {

    @Mapping(target = "message", expression = "java(orderDTO.getMessage() != null && !orderDTO.getMessage().isEmpty() ? orderDTO.getMessage() : null)")
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "voucher.id", source = "voucherId")
    Order toOrder(OrderDTO orderDTO);

    @Mapping(target = "message", expression = "java(order.getMessage() == null ? null : order.getMessage())")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "voucherId", source = "voucher.id")
    OrderDTO toOrderDTO(Order order);
}
