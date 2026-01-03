package ru.redeyed.cloudstorage.user;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.redeyed.cloudstorage.auth.UserDetailsImpl;
import ru.redeyed.cloudstorage.user.dto.CreateUserDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User toUser(CreateUserDto createUserDto);

    UserDetailsImpl toUserDetails(User user);
}
