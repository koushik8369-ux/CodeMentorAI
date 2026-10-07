package com.codementor.api.entity;

public enum Difficulty {
    EASY,
    MEDIUM,
    HARD;

    public static Difficulty fromString(String value) {
        if (value == null) return MEDIUM;
        for (Difficulty d : values()) {
            if (d.name().equalsIgnoreCase(value.trim())) {
                return d;
            }
        }
        return MEDIUM;
    }
}
