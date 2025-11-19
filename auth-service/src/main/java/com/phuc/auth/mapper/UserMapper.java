package com.phuc.auth.mapper;

import com.phuc.auth.dto.request.UserCreateRequest;
import com.phuc.auth.dto.request.UserUpdateRequest;
import com.phuc.auth.dto.response.UserResponse;
import com.phuc.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);   
}
