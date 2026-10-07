package com.codementor.api.entity;

public enum Language {
    JAVA,
    PYTHON,
    CPP,
    JAVASCRIPT;

    public static Language fromString(String value) {
        if (value == null) return JAVA;
        String normalized = value.trim().toUpperCase().replace("C++", "CPP");
        for (Language l : values()) {
            if (l.name().equals(normalized)) {
                return l;
            }
        }
        return JAVA;
    }
}
