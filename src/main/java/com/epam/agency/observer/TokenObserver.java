package com.epam.agency.observer;

import com.epam.agency.model.PasswordResetToken;
import com.epam.agency.repository.PasswordResetTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenObserver {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @PostConstruct
    public void init() {
        deleteOldTokens();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldTokens() {
        List<PasswordResetToken> allTokens = passwordResetTokenRepository.findAll();
        for (PasswordResetToken token : allTokens) {
            if (token.getExpiryDate().before(Date.from(Instant.now()))) {
                passwordResetTokenRepository.delete(token);
            }
        }
    }
}
