package photos.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents one tag attached to a photo.
 * A tag is stored as a name-value pair like person=maya or location=paris.
 *
 * @author Owner
 */
public final class Tag implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String value;

    /**
     * Creates a tag.
     *
     * @param name tag type name
     * @param value tag value
     */
    public Tag(String name, String value) {
        this.name = normalize(name);
        this.value = normalize(value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean matches(String otherName, String otherValue) {
        return name.equals(normalize(otherName)) && value.equals(normalize(otherValue));
    }

    public static String normalize(String raw) {
        return Objects.requireNonNull(raw, "raw").trim().toLowerCase();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Tag tag)) {
            return false;
        }
        return name.equals(tag.name) && value.equals(tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
