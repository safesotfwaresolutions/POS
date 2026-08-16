package com.sciencebot.pos.auth.internal.services;

import com.sciencebot.pos.config.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final ConcurrentHashMap<String, LockoutDetails> lockoutCache = new ConcurrentHashMap<>();

    public AuthService(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String username, String password) {
        if (username == null || password == null) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }
        
        String cleanUsername = username.trim().toLowerCase();

        checkLockout(cleanUsername);

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(cleanUsername);
        } catch (Exception e) {
            recordFailedAttempt(cleanUsername);
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        if (!userDetails.isEnabled()) {
            throw new DisabledException("Cuenta de usuario desactivada");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            recordFailedAttempt(cleanUsername);
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        lockoutCache.remove(cleanUsername);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("SELLER");

        String token = jwtService.generateToken(userDetails, role);

        return new LoginResponse(
                token,
                userDetails.getUsername(),
                role,
                jwtService.getExpirationTime()
        );
    }

    private void checkLockout(String username) {
        LockoutDetails details = lockoutCache.get(username);
        if (details != null && details.isLocked()) {
            throw new LockedException("Cuenta bloqueada temporalmente por intentos fallidos. Intente de nuevo más tarde.");
        }
    }

    private void recordFailedAttempt(String username) {
        lockoutCache.compute(username, (key, details) -> {
            long now = System.currentTimeMillis();
            if (details == null || now - details.firstAttemptTime > 15 * 60 * 1000) {
                return new LockoutDetails(1, now, 0);
            }

            int newCount = details.count + 1;
            long lockedUntil = 0;
            if (newCount >= 5) {
                lockedUntil = now + 30 * 60 * 1000;
            }
            return new LockoutDetails(newCount, details.firstAttemptTime, lockedUntil);
        });
    }

    private static class LockoutDetails {
        final int count;
        final long firstAttemptTime;
        final long lockedUntil;

        LockoutDetails(int count, long firstAttemptTime, long lockedUntil) {
            this.count = count;
            this.firstAttemptTime = firstAttemptTime;
            this.lockedUntil = lockedUntil;
        }

        boolean isLocked() {
            return lockedUntil > System.currentTimeMillis();
        }
    }

    public record LoginResponse(
            String token,
            String username,
            String role,
            long expiresIn
    ) {}
}
