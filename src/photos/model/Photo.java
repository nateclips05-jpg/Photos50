package photos.model;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a photo in the user's library.
 * The object stores the file path, the captured date taken from the file's
 * last modified time, the caption, and all tags attached to that photo.
 *
 * @author Owner
 */
public final class Photo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String path;
    private final LocalDateTime captureDate;
    private String caption;
    private final LinkedHashSet<Tag> tags = new LinkedHashSet<>();

    /**
     * Creates a photo entry.
     *
     * @param path absolute file path
     * @param captureDate filesystem last-modified date
     */
    public Photo(String path, LocalDateTime captureDate) {
        this.path = Objects.requireNonNull(path, "path");
        this.captureDate = Objects.requireNonNull(captureDate, "captureDate");
        this.caption = "";
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return Path.of(path).getFileName().toString();
    }

    public LocalDateTime getCaptureDate() {
        return captureDate;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = Objects.requireNonNullElse(caption, "").trim();
    }

    public Set<Tag> getTags() {
        return Set.copyOf(tags);
    }

    public boolean addTag(Tag tag) {
        return tags.add(Objects.requireNonNull(tag, "tag"));
    }

    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }

    public boolean hasTag(String name, String value) {
        return tags.stream().anyMatch(tag -> tag.matches(name, value));
    }

    public boolean hasTagType(String name) {
        String normalized = Tag.normalize(name);
        return tags.stream().anyMatch(tag -> tag.getName().equals(normalized));
    }

    @Override
    public String toString() {
        return getFileName();
    }
}
