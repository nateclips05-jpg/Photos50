package photos.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents one tag type a user can work with, such as location or person.
 * Each type also stores whether a photo is allowed to have more than one value
 * for that tag name.
 *
 * @author Owner
 */
public final class TagType implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final boolean multipleAllowed;

    /**
     * Creates a tag type.
     *
     * @param name tag type name
     * @param multipleAllowed whether photos may have multiple values for this type
     */
    public TagType(String name, boolean multipleAllowed) {
        this.name = normalize(name);
        this.multipleAllowed = multipleAllowed;
    }

    public String getName() {
        return name;
    }

    public boolean isMultipleAllowed() {
        return multipleAllowed;
    }

    public static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TagType that)) {
            return false;
        }
        return multipleAllowed == that.multipleAllowed && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, multipleAllowed);
    }

    @Override
    public String toString() {
        return name + (multipleAllowed ? " (multi)" : " (single)");
    }
}
