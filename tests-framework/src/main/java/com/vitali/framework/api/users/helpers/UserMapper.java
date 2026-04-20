package com.vitali.framework.api.users.helpers;

import com.vitali.framework.api.users.requests.UpdateUserRequest;
import com.vitali.framework.api.users.responses.GetUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "password", ignore = true)
    UpdateUserRequest toUpdateUserRequest(GetUserResponse response);
}
