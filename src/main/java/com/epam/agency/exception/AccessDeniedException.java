package com.epam.agency.exception;

public class AccessDeniedException extends BaseException {
    public AccessDeniedException(String statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
}
