package com.epam.agency.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final String statusCode;
    private final String statusMessage;

    public BaseException(String statusCode, String statusMessage) {
        super(statusMessage);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }
}
