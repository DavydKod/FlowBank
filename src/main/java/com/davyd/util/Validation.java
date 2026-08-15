package com.davyd.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validation {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private Validation() {
    }

    public static <T> T validateNotNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        return value;
    }

    public static String validateNotBlank(String value, String fieldName) {
        validateNotNull(value, fieldName);

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }

        return value;
    }

    public static String validateEmail(String email) {
        validateNotBlank(email, "Email");

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        return email;
    }

    public static BigDecimal validateBigDecimalNotNullAndPositive(BigDecimal amount){
        if (amount == null){
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Amount must be positive");
        }

        return amount;
    }
}