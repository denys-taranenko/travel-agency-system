package com.epam.agency.exception;

public class NotFoundException extends BaseException {
    public NotFoundException(String statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
}
