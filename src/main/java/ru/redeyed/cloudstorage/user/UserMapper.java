package ru.redeyed.cloudstorage.user;

import org.mapstruct.Mapper;
import ru.redeyed.cloudstorage.auth.UserDetailsImpl;

@Mapper
public interface UserMapper {

    User toUser(CreateUserDto createUserDto);

    UserDetailsImpl toUserDetails(User user);
}
