package com.epam.agency.model.enums;

public enum TransferType {
    BUS,
    TRAIN,
    PLANE,
    SHIP,
    PRIVATE_CAR,
    JEEPS,
    MINIBUS,
    ELECTRICAL_CARS;

    public String getFormattedName() {
        return name().replace("_", " ");
    }
}
