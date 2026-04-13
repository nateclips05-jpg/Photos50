package photos.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import photos.Photos;
import photos.model.Album;
import photos.model.Photo;
import photos.model.Tag;
import photos.model.TagType;
import photos.util.DialogUtils;
import photos.util.FormatUtils;
import photos.util.ImageUtils;
import photos.util.PhotoListCell;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Controls the screen for one open album.
 * This is where the user can add or remove photos, update captions, manage
 * tags, move or copy photos, and step through the album like a slideshow.
 *
 * @author Owner
 */
public final class AlbumController implements ScreenController {
    @FXML
    private Label albumNameLabel;
    @FXML
    private ListView<Photo> photoListView;
    @FXML
    private ImageView previewImageView;
    @FXML
    private Label photoNameLabel;
    @FXML
    private Label captionLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label tagsLabel;
    @FXML
    private ComboBox<TagType> tagTypeComboBox;
    @FXML
    private TextField tagValueField;

    private Photos app;
    private boolean initialized;

    @Override
    public void setApp(Photos app) {
        this.app = app;
    }

    @Override
    public void onShow() {
        if (!initialized) {
            photoListView.setCellFactory(list -> new PhotoListCell());
            photoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldPhoto, newPhoto) -> updateDetails(newPhoto));
            initialized = true;
        }
        refreshAlbum();
    }

    @FXML
    private void handleAddPhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Add Photo");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File file = chooser.showOpenDialog(photoListView.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            app.getService().addPhotoToAlbum(app.getCurrentUser(), app.getCurrentAlbum(), Path.of(file.toURI()));
            app.save();
            refreshAlbum();
        } catch (Exception exception) {
            DialogUtils.showError("Add Photo Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleRemovePhoto() {
        Photo photo = getSelectedPhoto();
        if (photo == null) {
            return;
        }
        if (!DialogUtils.confirm("Remove Photo", "Remove the selected photo from this album?")) {
            return;
        }
        try {
            app.getService().removePhotoFromAlbum(app.getCurrentUser(), app.getCurrentAlbum(), photo);
            app.save();
            refreshAlbum();
        } catch (Exception exception) {
            DialogUtils.showError("Remove Photo Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleRecaption() {
        Photo photo = getSelectedPhoto();
        if (photo == null) {
            return;
        }
        DialogUtils.promptTextAllowBlank("Update Caption", "Caption:", photo.getCaption())
                .ifPresent(caption -> {
                    try {
                        app.getService().updateCaption(photo, caption);
                        app.save();
                        refreshAlbum();
                        photoListView.getSelectionModel().select(photo);
                    } catch (Exception exception) {
                        DialogUtils.showError("Caption Failed", exception.getMessage());
                    }
                });
    }

    @FXML
    private void handleAddTagType() {
        Optional<String> name = DialogUtils.promptText("Create Tag Type", "Tag type name:", "");
        if (name.isEmpty()) {
            return;
        }
        boolean multipleAllowed = DialogUtils.confirm("Tag Type", "Should this tag type allow multiple values?");
        try {
            app.getService().createTagType(app.getCurrentUser(), name.get(), multipleAllowed);
            app.save();
            refreshTagTypes();
        } catch (Exception exception) {
            DialogUtils.showError("Create Tag Type Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleAddTag() {
        Photo photo = getSelectedPhoto();
        TagType tagType = tagTypeComboBox.getSelectionModel().getSelectedItem();
        if (photo == null) {
            return;
        }
        if (tagType == null) {
            DialogUtils.showError("Missing Tag Type", "Choose a tag type first.");
            return;
        }
        try {
            app.getService().addTag(app.getCurrentUser(), photo, tagType.getName(), tagValueField.getText());
            app.save();
            tagValueField.clear();
            updateDetails(photo);
        } catch (Exception exception) {
            DialogUtils.showError("Add Tag Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleDeleteTag() {
        Photo photo = getSelectedPhoto();
        if (photo == null) {
            return;
        }
        List<Tag> tags = photo.getTags().stream().sorted((a, b) -> a.toString().compareToIgnoreCase(b.toString())).toList();
        DialogUtils.promptChoice("Delete Tag", "Select a tag:", tags)
                .ifPresent(tag -> {
                    try {
                        app.getService().deleteTag(photo, tag);
                        app.save();
                        updateDetails(photo);
                    } catch (Exception exception) {
                        DialogUtils.showError("Delete Tag Failed", exception.getMessage());
                    }
                });
    }

    @FXML
    private void handleCopyPhoto() {
        Photo photo = getSelectedPhoto();
        if (photo == null) {
            return;
        }
        List<String> albums = app.getCurrentUser().getAlbums().stream()
                .map(Album::getName)
                .filter(name -> !name.equalsIgnoreCase(app.getCurrentAlbum().getName()))
                .toList();
        if (albums.isEmpty()) {
            DialogUtils.showError("No Target Album", "Create another album before copying a photo.");
            return;
        }
        DialogUtils.promptChoice("Copy Photo", "Target album:", albums)
                .ifPresent(name -> {
                    try {
                        app.getService().copyPhoto(app.getCurrentUser(), photo, name);
                        app.save();
                    } catch (Exception exception) {
                        DialogUtils.showError("Copy Failed", exception.getMessage());
                    }
                });
    }

    @FXML
    private void handleMovePhoto() {
        Photo photo = getSelectedPhoto();
        if (photo == null) {
            return;
        }
        List<String> albums = app.getCurrentUser().getAlbums().stream()
                .map(Album::getName)
                .filter(name -> !name.equalsIgnoreCase(app.getCurrentAlbum().getName()))
                .toList();
        if (albums.isEmpty()) {
            DialogUtils.showError("No Target Album", "Create another album before moving a photo.");
            return;
        }
        DialogUtils.promptChoice("Move Photo", "Target album:", albums)
                .ifPresent(name -> {
                    try {
                        app.getService().movePhoto(app.getCurrentUser(), app.getCurrentAlbum(), photo, name);
                        app.save();
                        refreshAlbum();
                    } catch (Exception exception) {
                        DialogUtils.showError("Move Failed", exception.getMessage());
                    }
                });
    }

    @FXML
    private void handlePrevious() {
        int index = photoListView.getSelectionModel().getSelectedIndex();
        if (index > 0) {
            photoListView.getSelectionModel().select(index - 1);
            photoListView.scrollTo(index - 1);
        }
    }

    @FXML
    private void handleNext() {
        int index = photoListView.getSelectionModel().getSelectedIndex();
        if (index < photoListView.getItems().size() - 1) {
            photoListView.getSelectionModel().select(index + 1);
            photoListView.scrollTo(index + 1);
        }
    }

    @FXML
    private void handleBack() {
        try {
            app.showUserHome();
        } catch (Exception exception) {
            DialogUtils.showError("Navigation Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            app.logout();
        } catch (Exception exception) {
            DialogUtils.showError("Logout Failed", exception.getMessage());
        }
    }

    private Photo getSelectedPhoto() {
        Photo photo = photoListView.getSelectionModel().getSelectedItem();
        if (photo == null) {
            DialogUtils.showError("No Selection", "Select a photo first.");
        }
        return photo;
    }

    private void refreshAlbum() {
        Album album = app.getCurrentAlbum();
        albumNameLabel.setText(album.getName());
        photoListView.setItems(FXCollections.observableArrayList(album.getPhotos()));
        refreshTagTypes();
        if (!photoListView.getItems().isEmpty()) {
            photoListView.getSelectionModel().selectFirst();
        } else {
            updateDetails(null);
        }
    }

    private void refreshTagTypes() {
        tagTypeComboBox.setItems(FXCollections.observableArrayList(app.getCurrentUser().getTagTypes()));
        if (!tagTypeComboBox.getItems().isEmpty()) {
            tagTypeComboBox.getSelectionModel().selectFirst();
        }
    }

    private void updateDetails(Photo photo) {
        if (photo == null) {
            previewImageView.setImage(null);
            photoNameLabel.setText("No photo selected");
            captionLabel.setText("");
            dateLabel.setText("");
            tagsLabel.setText("");
            return;
        }
        ImageUtils.configureImageView(previewImageView, photo, 420, 320);
        photoNameLabel.setText(photo.getFileName());
        captionLabel.setText(photo.getCaption().isBlank() ? "(no caption)" : photo.getCaption());
        dateLabel.setText(FormatUtils.formatDateTime(photo.getCaptureDate()));
        tagsLabel.setText(FormatUtils.formatTags(photo));
    }
}
