package photos.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Represents one album belonging to a user.
 * An album keeps references to photo objects, which lets the same photo appear
 * in multiple albums while still sharing one caption and one set of tags.
 *
 * @author Owner
 */
public final class Album implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private final ArrayList<Photo> photos = new ArrayList<>();

    /**
     * Creates an album.
     *
     * @param name album name
     */
    public Album(String name) {
        rename(name);
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        String normalized = Objects.requireNonNull(newName, "newName").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Album name is required.");
        }
        this.name = normalized;
    }

    public List<Photo> getPhotos() {
        return List.copyOf(photos);
    }

    public int getPhotoCount() {
        return photos.size();
    }

    public boolean containsPhoto(Photo photo) {
        return photos.contains(photo);
    }

    public boolean addPhoto(Photo photo) {
        Objects.requireNonNull(photo, "photo");
        if (photos.contains(photo)) {
            return false;
        }
        return photos.add(photo);
    }

    public boolean removePhoto(Photo photo) {
        return photos.remove(photo);
    }

    public LocalDateTime getEarliestDate() {
        return photos.stream()
                .map(Photo::getCaptureDate)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    public LocalDateTime getLatestDate() {
        return photos.stream()
                .map(Photo::getCaptureDate)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public String toString() {
        return name;
    }
}
