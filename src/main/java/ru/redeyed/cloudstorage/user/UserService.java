package ru.redeyed.cloudstorage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public void create(CreateUserDto createUserDto) {
        var user = userMapper.toUser(createUserDto);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException();
        }
    }
}
