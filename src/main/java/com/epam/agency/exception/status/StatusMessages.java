package com.epam.agency.exception.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusMessages {
    USER_NOT_FOUND("status.user.not.found"),
    USERNAME_ALREADY_EXISTS("status.user.already.exist"),
    PHONE_NUMBER_ALREADY_EXISTS("status.phone.already.exist"),
    EMAIL_ALREADY_EXISTS("status.email.exist"),
    WRONG_PASSWORD("status.wrong.password"),
    INVALID_PASSWORD("user.password.pattern"),
    ACCOUNT_BLOCKED("status.forbidden.blocked.user"),
    INVALID_TOKEN("status.invalid.token"),
    VOUCHER_NOT_FOUND("status.voucher.not.found"),
    BAD_AMOUNT("status.bad.amount"),
    VOUCHER_ALREADY_HAVE("status.voucher.already.have"),
    ORDER_NOT_FOUND("status.order.not.found"),
    NOT_ENOUGH_FUNDS("status.order.not.enough.funds"),
    LINK_EXPIRED("status.link.expired"),
    TOKEN_ALREADY_EXISTS("status.token.exist"),
    BAD_DELETING_ORDER("status.order.not.delete"),
    BAD_CANCELING_ORDER("status.order.canceling"),

    BASE_MESSAGE_BAD_REQUEST("error.bad.request");

    private final String statusMessage;
}
