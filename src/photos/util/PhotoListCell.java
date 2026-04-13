package photos.util;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import photos.model.Photo;

/**
 * Custom list cell used to display a photo preview in list views.
 * Each cell shows a thumbnail, file name, caption, and date for the photo.
 *
 * @author Owner
 */
public final class PhotoListCell extends ListCell<Photo> {
    @Override
    protected void updateItem(Photo photo, boolean empty) {
        super.updateItem(photo, empty);
        if (empty || photo == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        ImageView thumbnail = new ImageView();
        ImageUtils.configureImageView(thumbnail, photo, 120, 90);

        Label nameLabel = new Label(photo.getFileName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label captionLabel = new Label(photo.getCaption().isBlank() ? "(no caption)" : photo.getCaption());
        captionLabel.setWrapText(true);
        Label dateLabel = new Label(FormatUtils.formatDateTime(photo.getCaptureDate()));

        VBox textBox = new VBox(6, nameLabel, captionLabel, dateLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox container = new HBox(12, thumbnail, textBox);
        container.setPadding(new Insets(8));
        setText(null);
        setGraphic(container);
    }
}
