package com.epam.agency.model.enums;

public enum TourType {
    HEALTH,
    SPORTS,
    LEISURE,
    SAFARI,
    WINE,
    ECO,
    ADVENTURE,
    CULTURAL;

    public String getFormattedName() {
        return name().replace("_", " ");
    }
}
