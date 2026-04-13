package photos.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents one user account in the application.
 * A user owns albums, a personal photo library, and the list of tag types that
 * can be used while organizing photos.
 *
 * @author Owner
 */
public final class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final LinkedHashMap<String, Album> albums = new LinkedHashMap<>();
    private final LinkedHashMap<String, Photo> photoLibrary = new LinkedHashMap<>();
    private final LinkedHashMap<String, TagType> tagTypes = new LinkedHashMap<>();

    /**
     * Creates a user.
     *
     * @param username unique username
     */
    public User(String username) {
        this.username = normalizeUsername(username);
        registerTagType(new TagType("location", false));
        registerTagType(new TagType("person", true));
    }

    public String getUsername() {
        return username;
    }

    public Collection<Album> getAlbums() {
        return albums.values();
    }

    public Collection<Photo> getPhotoLibrary() {
        return photoLibrary.values();
    }

    public Collection<TagType> getTagTypes() {
        return tagTypes.values();
    }

    public boolean hasAlbum(String albumName) {
        return albums.containsKey(normalizeAlbumName(albumName));
    }

    public Album getAlbum(String albumName) {
        return albums.get(normalizeAlbumName(albumName));
    }

    public void addAlbum(Album album) {
        Objects.requireNonNull(album, "album");
        String key = normalizeAlbumName(album.getName());
        if (albums.containsKey(key)) {
            throw new IllegalArgumentException("Album already exists.");
        }
        albums.put(key, album);
    }

    public Album removeAlbum(String albumName) {
        return albums.remove(normalizeAlbumName(albumName));
    }

    public void renameAlbum(String currentName, String newName) {
        Album album = Objects.requireNonNull(getAlbum(currentName), "album");
        String newKey = normalizeAlbumName(newName);
        String oldKey = normalizeAlbumName(currentName);
        if (!oldKey.equals(newKey) && albums.containsKey(newKey)) {
            throw new IllegalArgumentException("Album already exists.");
        }
        albums.remove(oldKey);
        album.rename(newName);
        albums.put(newKey, album);
    }

    public Photo getPhoto(String pathKey) {
        return photoLibrary.get(normalizePath(pathKey));
    }

    public Photo putPhoto(Photo photo) {
        return photoLibrary.computeIfAbsent(normalizePath(photo.getPath()), key -> photo);
    }

    public void removePhoto(String pathKey) {
        photoLibrary.remove(normalizePath(pathKey));
    }

    public void registerTagType(TagType tagType) {
        tagTypes.put(TagType.normalize(tagType.getName()), tagType);
    }

    public TagType getTagType(String name) {
        return tagTypes.get(TagType.normalize(name));
    }

    public static String normalizeUsername(String username) {
        String normalized = Objects.requireNonNull(username, "username").trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        return normalized;
    }

    public static String normalizeAlbumName(String name) {
        String normalized = Objects.requireNonNull(name, "name").trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Album name is required.");
        }
        return normalized;
    }

    public static String normalizePath(String path) {
        return Objects.requireNonNull(path, "path").trim().toLowerCase();
    }
}
