package com.sciencebot.pos.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserFacade {
    Optional<UserDto> findByUsername(String username);
    UserDto createUser(CreateUserCommand command);
    UserDto updateUser(Long id, UpdateUserCommand command);
    void deleteUser(Long id);
    void changeStatus(Long id, boolean active);
    void changePassword(Long id, ChangePasswordCommand command);
    UserDto getById(Long id);
    Page<UserDto> listUsers(Pageable pageable);
    Page<UserDto> listUsersByRole(String role, Pageable pageable);
}
