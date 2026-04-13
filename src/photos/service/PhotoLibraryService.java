package photos.service;

import photos.model.Album;
import photos.model.AppState;
import photos.model.Photo;
import photos.model.Tag;
import photos.model.TagSearchOperator;
import photos.model.TagType;
import photos.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Central service layer for the application.
 * This class enforces the main rules of the project and gives the controllers
 * one place to call for user, album, photo, tag, search, and save operations.
 *
 * @author Owner
 */
public final class PhotoLibraryService {
    private final DataStore dataStore;
    private final AppState appState;

    /**
     * Creates a service bound to the current application state.
     *
     * @param dataStore persistence adapter
     * @param appState loaded state
     */
    public PhotoLibraryService(DataStore dataStore, AppState appState) {
        this.dataStore = dataStore;
        this.appState = appState;
    }

    public Collection<User> listUsers() {
        return appState.getUsers();
    }

    public User login(String username) {
        String normalized = User.normalizeUsername(username);
        User user = appState.getUser(normalized);
        if (user == null) {
            throw new IllegalArgumentException("Unknown user.");
        }
        return user;
    }

    public void createUser(String username) {
        String normalized = User.normalizeUsername(username);
        if (appState.hasUser(normalized)) {
            throw new IllegalArgumentException("User already exists.");
        }
        appState.addUser(new User(normalized));
    }

    public void deleteUser(String username) {
        String normalized = User.normalizeUsername(username);
        if ("admin".equals(normalized)) {
            throw new IllegalArgumentException("The admin user cannot be deleted.");
        }
        User removed = appState.removeUser(normalized);
        if (removed == null) {
            throw new IllegalArgumentException("User not found.");
        }
    }

    public Album createAlbum(User user, String albumName) {
        Album album = new Album(albumName);
        user.addAlbum(album);
        return album;
    }

    public void renameAlbum(User user, String currentName, String newName) {
        user.renameAlbum(currentName, newName);
    }

    public void deleteAlbum(User user, String albumName) {
        Album album = user.removeAlbum(albumName);
        if (album == null) {
            throw new IllegalArgumentException("Album not found.");
        }
        pruneOrphanedPhotos(user);
    }

    public Photo addPhotoToAlbum(User user, Album album, Path photoPath) throws IOException {
        if (!Files.exists(photoPath) || !Files.isRegularFile(photoPath)) {
            throw new IllegalArgumentException("Selected file does not exist.");
        }

        String lowerName = photoPath.getFileName().toString().toLowerCase();
        if (!(lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".gif") || lowerName.endsWith(".bmp"))) {
            throw new IllegalArgumentException("Supported formats are BMP, GIF, JPEG, and PNG.");
        }

        String absolutePath = photoPath.toAbsolutePath().toString();
        Photo photo = user.getPhoto(absolutePath);
        if (photo == null) {
            LocalDateTime captureDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(Files.getLastModifiedTime(photoPath).toMillis()),
                    ZoneId.systemDefault()
            ).truncatedTo(ChronoUnit.SECONDS);
            photo = user.putPhoto(new Photo(absolutePath, captureDate));
        }

        if (!album.addPhoto(photo)) {
            throw new IllegalArgumentException("That photo is already in the album.");
        }

        return photo;
    }

    public void removePhotoFromAlbum(User user, Album album, Photo photo) {
        if (!album.removePhoto(photo)) {
            throw new IllegalArgumentException("Photo not found in album.");
        }
        pruneOrphanedPhotos(user);
    }

    public void copyPhoto(User user, Photo photo, String targetAlbumName) {
        Album targetAlbum = requireAlbum(user, targetAlbumName);
        if (!targetAlbum.addPhoto(photo)) {
            throw new IllegalArgumentException("That photo is already in the target album.");
        }
    }

    public void movePhoto(User user, Album sourceAlbum, Photo photo, String targetAlbumName) {
        Album targetAlbum = requireAlbum(user, targetAlbumName);
        if (sourceAlbum == targetAlbum) {
            throw new IllegalArgumentException("Choose a different target album.");
        }
        if (!targetAlbum.addPhoto(photo)) {
            throw new IllegalArgumentException("That photo is already in the target album.");
        }
        sourceAlbum.removePhoto(photo);
        pruneOrphanedPhotos(user);
    }

    public void updateCaption(Photo photo, String caption) {
        photo.setCaption(caption);
    }

    public void createTagType(User user, String name, boolean multipleAllowed) {
        if (user.getTagType(name) != null) {
            throw new IllegalArgumentException("That tag type already exists.");
        }
        user.registerTagType(new TagType(name, multipleAllowed));
    }

    public void addTag(User user, Photo photo, String typeName, String value) {
        TagType tagType = user.getTagType(typeName);
        if (tagType == null) {
            throw new IllegalArgumentException("Unknown tag type.");
        }
        if (!tagType.isMultipleAllowed() && photo.hasTagType(typeName)) {
            throw new IllegalArgumentException("That tag type allows only one value.");
        }
        Tag tag = new Tag(typeName, value);
        if (!photo.addTag(tag)) {
            throw new IllegalArgumentException("That tag already exists on the photo.");
        }
    }

    public void deleteTag(Photo photo, Tag tag) {
        if (!photo.removeTag(tag)) {
            throw new IllegalArgumentException("Tag not found.");
        }
    }

    public List<Photo> searchByDate(User user, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Start and end dates are required.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Start date must not be after end date.");
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.SECONDS);

        return user.getPhotoLibrary().stream()
                .filter(photo -> !photo.getCaptureDate().isBefore(start) && !photo.getCaptureDate().isAfter(end))
                .toList();
    }

    public List<Photo> searchByTags(
            User user,
            String typeOne,
            String valueOne,
            TagSearchOperator operator,
            String typeTwo,
            String valueTwo
    ) {
        Objects.requireNonNull(operator, "operator");
        String normalizedTypeOne = requireText(typeOne, "First tag type is required.");
        String normalizedValueOne = requireText(valueOne, "First tag value is required.");

        return user.getPhotoLibrary().stream()
                .filter(photo -> matches(photo, normalizedTypeOne, normalizedValueOne, operator, typeTwo, valueTwo))
                .toList();
    }

    public Album createAlbumFromResults(User user, String albumName, List<Photo> photos) {
        Album album = createAlbum(user, albumName);
        for (Photo photo : photos) {
            album.addPhoto(photo);
        }
        return album;
    }

    public void save() throws IOException {
        dataStore.save(appState);
    }

    private boolean matches(
            Photo photo,
            String typeOne,
            String valueOne,
            TagSearchOperator operator,
            String typeTwo,
            String valueTwo
    ) {
        boolean first = photo.hasTag(typeOne, valueOne);
        return switch (operator) {
            case SINGLE -> first;
            case AND -> first && photo.hasTag(
                    requireText(typeTwo, "Second tag type is required."),
                    requireText(valueTwo, "Second tag value is required.")
            );
            case OR -> first || photo.hasTag(
                    requireText(typeTwo, "Second tag type is required."),
                    requireText(valueTwo, "Second tag value is required.")
            );
        };
    }

    private Album requireAlbum(User user, String albumName) {
        Album album = user.getAlbum(albumName);
        if (album == null) {
            throw new IllegalArgumentException("Album not found.");
        }
        return album;
    }

    private void pruneOrphanedPhotos(User user) {
        List<String> toRemove = new ArrayList<>();
        for (Photo photo : user.getPhotoLibrary()) {
            boolean referenced = user.getAlbums().stream().anyMatch(album -> album.containsPhoto(photo));
            if (!referenced) {
                toRemove.add(photo.getPath());
            }
        }
        for (String path : toRemove) {
            user.removePhoto(path);
        }
    }

    private String requireText(String text, String message) {
        String trimmed = Objects.requireNonNullElse(text, "").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }
}
