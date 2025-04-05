package com.epam.agency.repository;

import com.epam.agency.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    boolean existsByUserId(UUID userId);

    Optional<PasswordResetToken> findByToken(String token);
}
