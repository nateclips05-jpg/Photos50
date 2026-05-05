package com.example.rutgersphotos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class Album {
    private String name;
    private final ArrayList<PhotoItem> photos = new ArrayList<>();

    public Album(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String trimmed = Objects.requireNonNull(name, "name").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Album name is required.");
        }
        this.name = trimmed;
    }

    public List<PhotoItem> getPhotos() {
        return Collections.unmodifiableList(photos);
    }

    public PhotoItem getPhoto(int index) {
        return photos.get(index);
    }

    public void addPhoto(PhotoItem photo) {
        for (PhotoItem existing : photos) {
            if (existing.getUri().equals(photo.getUri())) {
                throw new IllegalArgumentException("That photo is already in this album.");
            }
        }
        photos.add(photo);
    }

    public PhotoItem removePhoto(int index) {
        return photos.remove(index);
    }

    public String normalizedName() {
        return normalizeName(name);
    }

    public static String normalizeName(String value) {
        String normalized = Objects.requireNonNull(value, "name").trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Album name is required.");
        }
        return normalized;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        JSONArray photoArray = new JSONArray();
        for (PhotoItem photo : photos) {
            photoArray.put(photo.toJson());
        }
        json.put("photos", photoArray);
        return json;
    }

    public static Album fromJson(JSONObject json) throws JSONException {
        Album album = new Album(json.getString("name"));
        JSONArray photoArray = json.optJSONArray("photos");
        if (photoArray != null) {
            for (int i = 0; i < photoArray.length(); i++) {
                album.photos.add(PhotoItem.fromJson(photoArray.getJSONObject(i)));
            }
        }
        return album;
    }
}
