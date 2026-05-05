package com.example.rutgersphotos;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

public final class Tag {
    public static final String PERSON = "person";
    public static final String LOCATION = "location";

    private final String type;
    private final String value;

    public Tag(String type, String value) {
        this.type = normalizeType(type);
        this.value = normalizeValue(value);
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean matchesExact(String requestedType, String requestedValue) {
        return type.equals(normalizeType(requestedType)) && value.equals(normalizeValue(requestedValue));
    }

    public boolean matchesPrefix(String requestedType, String requestedPrefix) {
        return type.equals(normalizeType(requestedType)) && value.startsWith(normalizeValue(requestedPrefix));
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("value", value);
        return json;
    }

    public static Tag fromJson(JSONObject json) throws JSONException {
        return new Tag(json.getString("type"), json.getString("value"));
    }

    public static String normalizeType(String raw) {
        String normalized = normalizeValue(raw);
        if (!PERSON.equals(normalized) && !LOCATION.equals(normalized)) {
            throw new IllegalArgumentException("Tag type must be person or location.");
        }
        return normalized;
    }

    public static String normalizeValue(String raw) {
        String normalized = Objects.requireNonNull(raw, "value").trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Tag value is required.");
        }
        return normalized;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Tag)) {
            return false;
        }
        Tag tag = (Tag) other;
        return type.equals(tag.type) && value.equals(tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return type + "=" + value;
    }
}
