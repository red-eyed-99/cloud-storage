package ru.redeyed.cloudstorage.auth;

import org.mapstruct.Mapper;
import ru.redeyed.cloudstorage.auth.dto.SignUpRequestDto;
import ru.redeyed.cloudstorage.user.CreateUserDto;

@Mapper
public interface AuthUserMapper {

    CreateUserDto toCreateUserDto(SignUpRequestDto signUpRequestDto);
}
