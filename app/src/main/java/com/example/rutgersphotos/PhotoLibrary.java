package com.example.rutgersphotos;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public final class PhotoLibrary {
    private static final String FILE_NAME = "photo-library.json";

    private final ArrayList<Album> albums = new ArrayList<>();

    public List<Album> getAlbums() {
        return Collections.unmodifiableList(albums);
    }

    public Album getAlbum(int index) {
        return albums.get(index);
    }

    public Album createAlbum(String name) {
        ensureAlbumNameAvailable(name, -1);
        Album album = new Album(name);
        albums.add(album);
        return album;
    }

    public void renameAlbum(int index, String name) {
        ensureAlbumNameAvailable(name, index);
        albums.get(index).setName(name);
    }

    public void deleteAlbum(int index) {
        albums.remove(index);
    }

    public void movePhoto(int sourceAlbumIndex, int photoIndex, int targetAlbumIndex) {
        if (sourceAlbumIndex == targetAlbumIndex) {
            throw new IllegalArgumentException("Choose a different album.");
        }
        Album source = albums.get(sourceAlbumIndex);
        Album target = albums.get(targetAlbumIndex);
        PhotoItem photo = source.getPhoto(photoIndex);
        target.addPhoto(photo);
        source.removePhoto(photoIndex);
    }

    public List<SearchResult> search(String typeOne, String valueOne, String operator, String typeTwo, String valueTwo) {
        ArrayList<SearchResult> results = new ArrayList<>();
        for (int albumIndex = 0; albumIndex < albums.size(); albumIndex++) {
            Album album = albums.get(albumIndex);
            for (int photoIndex = 0; photoIndex < album.getPhotos().size(); photoIndex++) {
                PhotoItem photo = album.getPhoto(photoIndex);
                boolean first = photo.hasTagPrefix(typeOne, valueOne);
                boolean match;
                if ("AND".equals(operator)) {
                    match = first && photo.hasTagPrefix(typeTwo, valueTwo);
                } else if ("OR".equals(operator)) {
                    match = first || photo.hasTagPrefix(typeTwo, valueTwo);
                } else {
                    match = first;
                }
                if (match) {
                    results.add(new SearchResult(albumIndex, photoIndex, album.getName(), photo));
                }
            }
        }
        return results;
    }

    public List<String> suggestionsFor(String type, String prefix) {
        String normalizedType = Tag.normalizeType(type);
        String normalizedPrefix = Tag.normalizeValue(prefix);
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (Album album : albums) {
            for (PhotoItem photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    if (tag.getType().equals(normalizedType) && tag.getValue().startsWith(normalizedPrefix)) {
                        values.add(tag.getValue());
                    }
                }
            }
        }
        return new ArrayList<>(values);
    }

    public void save(Context context) throws IOException, JSONException {
        JSONObject root = new JSONObject();
        JSONArray albumArray = new JSONArray();
        for (Album album : albums) {
            albumArray.put(album.toJson());
        }
        root.put("albums", albumArray);
        File file = new File(context.getFilesDir(), FILE_NAME);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(root.toString(2));
        }
    }

    public static PhotoLibrary load(Context context) throws IOException, JSONException {
        PhotoLibrary library = new PhotoLibrary();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return library;
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        JSONObject root = new JSONObject(builder.toString());
        JSONArray albumArray = root.optJSONArray("albums");
        if (albumArray != null) {
            for (int i = 0; i < albumArray.length(); i++) {
                library.albums.add(Album.fromJson(albumArray.getJSONObject(i)));
            }
        }
        return library;
    }

    private void ensureAlbumNameAvailable(String name, int allowedIndex) {
        String normalized = Album.normalizeName(name);
        for (int i = 0; i < albums.size(); i++) {
            if (i != allowedIndex && albums.get(i).normalizedName().equals(normalized)) {
                throw new IllegalArgumentException("Album already exists.");
            }
        }
    }
}
