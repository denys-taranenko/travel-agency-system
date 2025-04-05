package com.epam.agency.security;

import com.epam.agency.dto.UserDTO;
import com.epam.agency.exception.AlreadyExistsException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.exception.status.StatusCodes;
import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.mapper.UserMapper;
import com.epam.agency.model.PasswordResetToken;
import com.epam.agency.model.User;
import com.epam.agency.repository.PasswordResetTokenRepository;
import com.epam.agency.repository.UserRepository;
import com.epam.agency.util.RegexConstant;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public void createPasswordResetTokenForUser(UserDTO userDTO, String token) throws MessagingException {
        User user = userMapper.toUser(userDTO);

        if (tokenRepository.existsByUserId(user.getId())) {
            throw new AlreadyExistsException(StatusCodes.CONFLICT.name(), StatusMessages.TOKEN_ALREADY_EXISTS.getStatusMessage());
        }

        PasswordResetToken myToken = new PasswordResetToken(token, user);
        tokenRepository.save(myToken);
        sendResetEmail(user.getEmail(), token);
    }

    private void sendResetEmail(String email, String token) throws MessagingException {
        String frontendUrl = "https://localhost:8080";
        String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, token);
        String subject = "Password Reset Request";

        String content = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333; background-color: #f4f4f9; margin: 0; padding: 0;">
                        <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);">
                            <h2 style="color: #0056b3; font-size: 24px; text-align: center;">Password Reset Request</h2>
                            <p style="font-size: 16px; color: #555; line-height: 1.5;">Hi,</p>
                            <p style="font-size: 16px; color: #555; line-height: 1.5;">
                                You have requested a password reset. Please click the button below to reset your password. If you did not request this, you can ignore this email.
                            </p>
                            <div style="text-align: center; margin: 20px 0;">
                                <a href="%s" style="background-color: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: bold; transition: background-color 0.3s ease;">Reset Password</a>
                            </div>
                            <p style="font-size: 16px; color: #555; line-height: 1.5;">
                                If the button doesn't work, you can copy and paste the following link into your browser:
                            </p>
                            <p style="font-size: 16px; color: #007bff; word-wrap: break-word; word-break: break-word;">
                                <a href="%s">%s</a>
                            </p>
                            <hr style="border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                            <p style="font-size: 12px; color: #777; text-align: center;">
                                This is an automated message. Please do not reply.
                            </p>
                        </div>
                    </body>
                    </html>
                """.formatted(resetLink, resetLink, resetLink);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElseThrow(() ->
                new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.INVALID_TOKEN.getStatusMessage()));

        if (!newPassword.matches(RegexConstant.PASSWORD_REGEX)) {
            throw new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.INVALID_PASSWORD.getStatusMessage());
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    public String validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElseThrow(() ->
                new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.INVALID_TOKEN.getStatusMessage()));

        if (resetToken.getExpiryDate().before(new Date())) {
            throw new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.LINK_EXPIRED.getStatusMessage());
        }

        return "valid";
    }
}
