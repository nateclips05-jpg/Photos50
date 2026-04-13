package photos.util;

import photos.model.Album;
import photos.model.Photo;
import photos.model.Tag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Utility methods for turning dates and metadata into readable text.
 * These helpers are mainly used by the UI so album ranges and tag lists are
 * displayed in a cleaner way.
 *
 * @author Owner
 */
public final class FormatUtils {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FormatUtils() {
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    public static String formatAlbumRange(Album album) {
        if (album.getPhotoCount() == 0) {
            return "No photos";
        }
        return formatDateTime(album.getEarliestDate()) + " to " + formatDateTime(album.getLatestDate());
    }

    public static String formatTags(Photo photo) {
        if (photo.getTags().isEmpty()) {
            return "No tags";
        }
        return photo.getTags().stream()
                .sorted(Comparator.comparing(Tag::getName).thenComparing(Tag::getValue))
                .map(Tag::toString)
                .collect(Collectors.joining(", "));
    }
}
