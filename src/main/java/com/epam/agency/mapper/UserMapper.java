package com.epam.agency.mapper;

import com.epam.agency.dto.UserDTO;
import com.epam.agency.model.User;
import com.epam.agency.model.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;

@Mapper(componentModel = "spring", uses = OrderMapper.class, imports = {UUID.class, Role.class},
        injectionStrategy = CONSTRUCTOR)
public interface UserMapper {

    @Mapping(target = "email", expression = "java(userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty() ? userDTO.getEmail() : null)")
    @Mapping(target = "phoneNumber", expression = "java(userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().trim().isEmpty() ? userDTO.getPhoneNumber() : null)")
    User toUser(UserDTO userDTO);

    @Mapping(target = "email", expression = "java(user.getEmail() != null ? user.getEmail() : null)")
    @Mapping(target = "phoneNumber", expression = "java(user.getPhoneNumber() != null ? user.getPhoneNumber() : null)")
    UserDTO toUserDTO(User user);
}
