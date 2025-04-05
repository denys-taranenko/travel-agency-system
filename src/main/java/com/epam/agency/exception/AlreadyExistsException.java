package com.epam.agency.exception;

public class AlreadyExistsException extends BaseException {
    public AlreadyExistsException(String statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
}
