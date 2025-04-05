package com.epam.agency.payment;

import com.epam.agency.dto.UserDTO;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.exception.status.StatusCodes;
import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.service.UserService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripeService {
    private final UserService userService;

    @Value("${stripe.key.secret}")
    private String secretKey;

    public Charge charge(String token, BigDecimal amount, String currency, String description) {
        try {
            Stripe.apiKey = secretKey;

            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount(amount.multiply(new BigDecimal(100)).longValue())
                    .setCurrency(currency.toLowerCase())
                    .setSource(token)
                    .setDescription(description)
                    .build();

            return Charge.create(params);
        } catch (StripeException exception) {
            throw new InvalidDataException(StatusCodes.NOT_FOUND.name(), StatusMessages.NOT_ENOUGH_FUNDS.getStatusMessage());
        }
    }

    public Charge topUpBalance(String token, BigDecimal amount, String currency, UUID userId) {
        try {
            Stripe.apiKey = secretKey;

            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount(amount.multiply(new BigDecimal(100)).longValue())
                    .setCurrency(currency.toLowerCase())
                    .setSource(token)
                    .setDescription("Top up balance for user ID: " + userId)
                    .build();

            Charge charge = Charge.create(params);
            UserDTO user = userService.getUserDTOById(String.valueOf(userId));

            if (charge.getPaid()) {
                userService.changeUserBalance(user, amount);
            }

            return charge;
        } catch (StripeException exception) {
            throw new InvalidDataException(StatusCodes.BAD_REQUEST.name(), StatusMessages.BAD_AMOUNT.getStatusMessage());
        }
    }
}
