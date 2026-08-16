package com.sciencebot.pos.users.internal.repositories;

import com.sciencebot.pos.users.internal.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByRoleAndActiveTrue(String role);
    Page<User> findAllByActiveTrue(Pageable pageable);
}
