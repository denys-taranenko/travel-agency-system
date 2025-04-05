package com.epam.agency.model.enums;

public enum OrderStatus {
    REGISTERED,
    APPROVED,
    PAID,
    CANCELED,
    DELETED,
    PROCESSED,
    PAID_AND_CANCELED;

    public String getFormattedName() {
        return name().replace("_", " ");
    }
}
