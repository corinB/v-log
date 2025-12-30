package com.likelion.vlog.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum TagMode {
    AND, OR, NAND;

    @JsonCreator
    public static TagMode from(String value) {
        if (value == null) return null;
        return TagMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}