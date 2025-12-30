package com.likelion.vlog.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum SortField {
    VIEW, LIKE, CREATED_AT, UPDATED_AT;

    @JsonCreator
    public static SortField from(String value) {
        if (value == null) return null;
        return SortField.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }

}
