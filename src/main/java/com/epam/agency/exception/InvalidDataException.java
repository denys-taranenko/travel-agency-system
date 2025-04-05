package com.epam.agency.exception;

public class InvalidDataException extends BaseException {
    public InvalidDataException(String statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
}
