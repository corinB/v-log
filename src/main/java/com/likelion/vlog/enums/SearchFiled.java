package com.likelion.vlog.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.boot.jackson.JsonComponent;

import java.util.Locale;

public enum SearchFiled {
    BLOG,NICKNAME,TITLE;

    @JsonCreator
    public static SearchFiled from(String value) {
        if (value == null) return null;
        return SearchFiled.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
