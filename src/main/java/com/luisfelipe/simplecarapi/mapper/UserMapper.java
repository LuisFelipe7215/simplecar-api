package com.luisfelipe.simplecarapi.mapper;

import com.luisfelipe.simplecarapi.annotation.EncodedMapping;
import com.luisfelipe.simplecarapi.domain.User;
import com.luisfelipe.simplecarapi.request.UserPostRequest;
import com.luisfelipe.simplecarapi.response.UserPostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PasswordEncoderMapper.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", constant = "USER")
    @Mapping(target = "password", qualifiedBy = EncodedMapping.class)
    User toUser(UserPostRequest userPostRequest);

    UserPostResponse toUserPostResponse(User user);
}
