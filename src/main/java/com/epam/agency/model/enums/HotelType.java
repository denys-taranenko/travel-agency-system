package com.epam.agency.model.enums;

public enum HotelType {
    ONE_STAR,
    TWO_STARS,
    THREE_STARS,
    FOUR_STARS,
    FIVE_STARS;

    public String getFormattedName() {
        return name().replace("_", " ");
    }
}
