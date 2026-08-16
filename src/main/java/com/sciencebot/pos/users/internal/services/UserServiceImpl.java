package com.sciencebot.pos.users.internal.services;

import com.sciencebot.pos.users.*;
import com.sciencebot.pos.users.internal.entities.User;
import com.sciencebot.pos.users.internal.repositories.UserRepository;
import com.sciencebot.pos.users.internal.mappers.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserFacade, UserDetailsService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMINISTRATOR", "SUPERVISOR", "SELLER");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(
            UserRepository userRepository,
            @org.springframework.context.annotation.Lazy PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isActive())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
                .build();
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserCommand command) {
        validateRole(command.role());
        validatePasswordStrength(command.password());

        if (userRepository.existsByUsername(command.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        User user = new User();
        user.setFullName(command.fullName());
        user.setUsername(command.username());
        user.setEmail(command.email());
        user.setPassword(passwordEncoder.encode(command.password()));
        user.setRole(command.role().toUpperCase());
        user.setActive(true);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UpdateUserCommand command) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        validateRole(command.role());

        if (!user.getEmail().equalsIgnoreCase(command.email()) && userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        // RN-USER-002: If changing role from ADMINISTRATOR to something else, check if it's the last Admin
        if (user.getRole().equals("ADMINISTRATOR") && !command.role().equalsIgnoreCase("ADMINISTRATOR")) {
            long adminCount = userRepository.countByRoleAndActiveTrue("ADMINISTRATOR");
            if (adminCount <= 1) {
                throw new IllegalArgumentException("No se puede cambiar el rol del último administrador activo");
            }
        }

        user.setFullName(command.fullName());
        user.setEmail(command.email());
        user.setRole(command.role().toUpperCase());

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        // Admin cannot delete self
        String currentUsername = getCurrentUsername();
        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new IllegalArgumentException("No puedes eliminar a tu propio usuario");
        }

        // RN-USER-002: Cannot delete last administrator
        if (user.getRole().equals("ADMINISTRATOR")) {
            long adminCount = userRepository.countByRoleAndActiveTrue("ADMINISTRATOR");
            if (adminCount <= 1) {
                throw new IllegalArgumentException("No se puede eliminar al último administrador activo");
            }
        }

        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        // Admin cannot deactivate self
        String currentUsername = getCurrentUsername();
        if (user.getUsername().equalsIgnoreCase(currentUsername) && !active) {
            throw new IllegalArgumentException("No puedes desactivar a tu propio usuario");
        }

        // RN-USER-002: Cannot deactivate last administrator
        if (user.getRole().equals("ADMINISTRATOR") && !active) {
            long adminCount = userRepository.countByRoleAndActiveTrue("ADMINISTRATOR");
            if (adminCount <= 1) {
                throw new IllegalArgumentException("No se puede desactivar al último administrador activo");
            }
        }

        user.setActive(active);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordCommand command) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        String currentUsername = getCurrentUsername();
        
        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            if (command.currentPassword() == null || command.currentPassword().isBlank()) {
                throw new IllegalArgumentException("La contraseña actual es requerida para cambiar tu contraseña");
            }
            if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("La contraseña actual es incorrecta");
            }
        } else {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                throw new org.springframework.security.access.AccessDeniedException("No autenticado");
            }
            boolean isAdminOrSupervisor = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR") || a.getAuthority().equals("ROLE_SUPERVISOR"));
            if (!isAdminOrSupervisor) {
                throw new org.springframework.security.access.AccessDeniedException("No tiene permisos para restablecer la contraseña de otro usuario");
            }
        }

        validatePasswordStrength(command.newPassword());
        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
    }

    @Override
    public UserDto getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public Page<UserDto> listUsers(Pageable pageable) {
        return userRepository.findAllByActiveTrue(pageable)
                .map(userMapper::toDto);
    }


    private void validateRole(String role) {
        if (role == null || !ALLOWED_ROLES.contains(role.toUpperCase())) {
            throw new IllegalArgumentException("Rol inválido. Roles permitidos: ADMINISTRATOR, SUPERVISOR, SELLER");
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una letra y un número");
        }
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return auth.getName();
    }
}
