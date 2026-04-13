package photos.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import photos.model.Photo;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Helper methods for loading images into JavaFX controls.
 * This keeps image setup in one place instead of repeating the same code in
 * multiple controllers and custom cells.
 *
 * @author Owner
 */
public final class ImageUtils {
    private ImageUtils() {
    }

    public static Image loadImage(Photo photo, double width, double height) {
        Path path = Path.of(photo.getPath());
        if (!Files.exists(path)) {
            return null;
        }
        return new Image(path.toUri().toString(), width, height, true, true, true);
    }

    public static void configureImageView(ImageView imageView, Photo photo, double width, double height) {
        Image image = loadImage(photo, width, height);
        imageView.setImage(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
    }
}
