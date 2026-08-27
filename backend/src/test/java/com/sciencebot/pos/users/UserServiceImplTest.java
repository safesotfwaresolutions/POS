package com.sciencebot.pos.users;

import com.sciencebot.pos.users.internal.entities.User;
import com.sciencebot.pos.users.internal.repositories.UserRepository;
import com.sciencebot.pos.users.internal.services.UserServiceImpl;
import com.sciencebot.pos.users.internal.mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_Success() {
        CreateUserCommand command = new CreateUserCommand(
                "Juan Perez", "juanp", "juan@tienda.com", "Password123", "SELLER", 1L
        );

        when(userRepository.existsByUsername("juanp")).thenReturn(false);
        when(userRepository.existsByEmail("juan@tienda.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName("Juan Perez");
        savedUser.setUsername("juanp");
        savedUser.setEmail("juan@tienda.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole("SELLER");
        savedUser.setActive(true);
        savedUser.setStoreId(1L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto expectedDto = new UserDto(1L, "Juan Perez", "juanp", "juan@tienda.com", "SELLER", true, 1L, true);
        when(userMapper.toDto(any(User.class))).thenReturn(expectedDto);

        UserDto result = userService.createUser(command);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("juanp", result.username());
        assertTrue(result.active());
        assertEquals(1L, result.storeId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WeakPassword_ThrowsException() {
        CreateUserCommand command = new CreateUserCommand(
                "Juan Perez", "juanp", "juan@tienda.com", "weak", "SELLER", 1L
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.createUser(command));
        assertTrue(ex.getMessage().contains("caracteres"));
    }

    @Test
    void createUser_SuperAdminWithStoreId_ThrowsException() {
        CreateUserCommand command = new CreateUserCommand(
                "Super Admin", "superadmin", "sa@platform.com", "SuperAdmin1!", "SUPER_ADMIN", 1L
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.createUser(command));
        assertTrue(ex.getMessage().contains("SUPER_ADMIN"));
    }

    @Test
    void createUser_PosRoleWithoutStoreId_ThrowsException() {
        CreateUserCommand command = new CreateUserCommand(
                "Juan Perez", "juanp", "juan@tienda.com", "Password123", "SELLER", null
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.createUser(command));
        assertTrue(ex.getMessage().contains("storeId"));
    }

    @Test
    void deleteUser_LastAdmin_ThrowsException() {
        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setUsername("admin2");
        targetUser.setRole("ADMINISTRATOR");
        targetUser.setActive(true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin1");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.countByRoleAndActiveTrue("ADMINISTRATOR")).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(2L));
        assertTrue(ex.getMessage().contains("ultimo administrador"));
    }

    @Test
    void changePassword_Self_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("juanp");
        user.setPassword("oldEncodedPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123", "oldEncodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("newEncodedPassword");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("juanp");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        ChangePasswordCommand command = new ChangePasswordCommand("OldPassword123", "NewPassword123");
        userService.changePassword(1L, command);

        assertEquals("newEncodedPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePassword_Self_IncorrectCurrentPassword_ThrowsException() {
        User user = new User();
        user.setId(1L);
        user.setUsername("juanp");
        user.setPassword("oldEncodedPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "oldEncodedPassword")).thenReturn(false);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("juanp");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        ChangePasswordCommand command = new ChangePasswordCommand("WrongPassword", "NewPassword123");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1L, command));
        assertTrue(ex.getMessage().contains("incorrecta"));
    }

    @Test
    void changePassword_AdminReset_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("juanp");
        user.setPassword("oldEncodedPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword123")).thenReturn("newEncodedPassword");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin1");
        org.springframework.security.core.authority.SimpleGrantedAuthority authority =
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMINISTRATOR");
        doReturn(java.util.List.of(authority)).when(auth).getAuthorities();

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        ChangePasswordCommand command = new ChangePasswordCommand(null, "NewPassword123");
        userService.changePassword(1L, command);

        assertEquals("newEncodedPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePassword_SellerResetOther_ThrowsException() {
        User user = new User();
        user.setId(1L);
        user.setUsername("juanp");
        user.setPassword("oldEncodedPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("seller1");
        org.springframework.security.core.authority.SimpleGrantedAuthority authority =
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SELLER");
        doReturn(java.util.List.of(authority)).when(auth).getAuthorities();

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        ChangePasswordCommand command = new ChangePasswordCommand(null, "NewPassword123");
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> userService.changePassword(1L, command));
    }

    @Test
    void registerPendingAdmin_Success_CreatesUnverifiedAdminWithoutStore() {
        RegisterOwnerCommand command = new RegisterOwnerCommand("Carlos Martinez", "carlos.m", "carlos@empresa.com", "Password123");

        when(userRepository.existsByUsername("carlos.m")).thenReturn(false);
        when(userRepository.existsByEmail("carlos@empresa.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(3L);
        savedUser.setRole("ADMINISTRATOR");
        savedUser.setEmailVerified(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto expectedDto = new UserDto(3L, "Carlos Martinez", "carlos.m", "carlos@empresa.com", "ADMINISTRATOR", true, null, false);
        when(userMapper.toDto(any(User.class))).thenReturn(expectedDto);

        UserDto result = userService.registerPendingAdmin(command);

        assertNotNull(result);
        assertFalse(result.emailVerified());
        assertNull(result.storeId());

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("ADMINISTRATOR", captor.getValue().getRole());
        assertFalse(captor.getValue().isEmailVerified());
        assertNull(captor.getValue().getStoreId());
    }

    @Test
    void registerPendingAdmin_DuplicateUsername_ThrowsException() {
        RegisterOwnerCommand command = new RegisterOwnerCommand("Carlos Martinez", "carlos.m", "carlos@empresa.com", "Password123");
        when(userRepository.existsByUsername("carlos.m")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.registerPendingAdmin(command));
        assertTrue(ex.getMessage().contains("usuario ya esta registrado"));
    }

    @Test
    void assignStore_UserAlreadyHasStore_ThrowsException() {
        User user = new User();
        user.setId(3L);
        user.setStoreId(1L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.assignStore(3L, 2L));
        assertTrue(ex.getMessage().contains("ya tiene un local"));
    }

    @Test
    void assignStore_Success() {
        User user = new User();
        user.setId(3L);
        user.setStoreId(null);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new UserDto(u.getId(), null, null, null, null, true, u.getStoreId(), true);
        });

        UserDto result = userService.assignStore(3L, 9L);

        assertEquals(9L, result.storeId());
    }

    @Test
    void markEmailVerified_Success() {
        User user = new User();
        user.setId(3L);
        user.setEmailVerified(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new UserDto(u.getId(), null, null, null, null, true, u.getStoreId(), u.isEmailVerified());
        });

        UserDto result = userService.markEmailVerified(3L);

        assertTrue(result.emailVerified());
        assertTrue(user.isEmailVerified());
    }
}