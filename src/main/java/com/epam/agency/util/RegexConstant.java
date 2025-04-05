package com.epam.agency.util;

public final class RegexConstant {
    public static final String USERNAME_REGEX = "^[A-Za-z0-9_-]{4,20}$";
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{7,30}$";
    public static final String PHONE_NUMBER_REGEX = "^\\+?3809\\d{8}$|^80\\d{9}$|^(\\+?38)?0\\d{9}$";

    private RegexConstant() {
    }
}
