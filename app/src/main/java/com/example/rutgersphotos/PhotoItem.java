package com.example.rutgersphotos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PhotoItem {
    private final String uri;
    private final ArrayList<Tag> tags = new ArrayList<>();

    public PhotoItem(String uri) {
        this.uri = Objects.requireNonNull(uri, "uri");
    }

    public String getUri() {
        return uri;
    }

    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public boolean addTag(Tag tag) {
        if (tags.contains(tag)) {
            return false;
        }
        if (Tag.LOCATION.equals(tag.getType()) && hasTagType(Tag.LOCATION)) {
            throw new IllegalArgumentException("A photo can have only one location tag.");
        }
        return tags.add(tag);
    }

    public void removeTag(int index) {
        tags.remove(index);
    }

    public boolean hasTagType(String type) {
        String normalizedType = Tag.normalizeType(type);
        for (Tag tag : tags) {
            if (tag.getType().equals(normalizedType)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTagPrefix(String type, String valuePrefix) {
        for (Tag tag : tags) {
            if (tag.matchesPrefix(type, valuePrefix)) {
                return true;
            }
        }
        return false;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("uri", uri);
        JSONArray tagArray = new JSONArray();
        for (Tag tag : tags) {
            tagArray.put(tag.toJson());
        }
        json.put("tags", tagArray);
        return json;
    }

    public static PhotoItem fromJson(JSONObject json) throws JSONException {
        PhotoItem photo = new PhotoItem(json.getString("uri"));
        JSONArray tagArray = json.optJSONArray("tags");
        if (tagArray != null) {
            for (int i = 0; i < tagArray.length(); i++) {
                photo.tags.add(Tag.fromJson(tagArray.getJSONObject(i)));
            }
        }
        return photo;
    }
}
