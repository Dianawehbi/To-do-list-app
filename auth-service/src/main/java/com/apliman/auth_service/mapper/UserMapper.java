package com.apliman.auth_service.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.apliman.auth_service.DTO.request.UserRequestDTO;
import com.apliman.auth_service.DTO.response.UserResponseDTO;
import com.apliman.auth_service.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toDto(User user);

    List<UserResponseDTO> toDtoList(List<User> users);

    void updateEntityFromDto(UserRequestDTO dto, @MappingTarget User user);
}
