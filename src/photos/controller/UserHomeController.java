package photos.controller;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import photos.Photos;
import photos.model.Album;
import photos.util.DialogUtils;
import photos.util.FormatUtils;

/**
 * Controls the main album screen for a normal user.
 * It shows all of the user's albums and provides the actions for creating,
 * renaming, deleting, opening, and searching albums.
 *
 * @author Owner
 */
public final class UserHomeController implements ScreenController {
    @FXML
    private TableView<Album> albumTable;
    @FXML
    private TableColumn<Album, String> nameColumn;
    @FXML
    private TableColumn<Album, Number> countColumn;
    @FXML
    private TableColumn<Album, String> rangeColumn;

    private Photos app;
    private boolean columnsInitialized;

    @Override
    public void setApp(Photos app) {
        this.app = app;
    }

    @Override
    public void onShow() {
        if (!columnsInitialized) {
            nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
            countColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getPhotoCount()));
            rangeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(FormatUtils.formatAlbumRange(data.getValue())));
            columnsInitialized = true;
        }
        refreshAlbums();
    }

    @FXML
    private void handleCreateAlbum() {
        DialogUtils.promptText("Create Album", "Album name:", "")
                .ifPresent(name -> {
                    try {
                        app.getService().createAlbum(app.getCurrentUser(), name);
                        app.save();
                        refreshAlbums();
                    } catch (Exception exception) {
                        DialogUtils.showError("Create Album Failed", exception.getMessage());
                    }
                });
    }

    @FXML
    private void handleRenameAlbum() {
        Album album = getSelectedAlbum();
        if (album == null) {
            return;
        }
        DialogUtils.promptText("Rename Album", "New album name:", album.getName())
                .ifPresent(name -> {
                    try {
                        app.getService().renameAlbum(app.getCurrentUser(), album.getName(), name);
                        app.save();
                        refreshAlbums();
                    } catch (Exception exception) {
                        DialogUtils.showError("Rename Album Failed", exception.getMessage());
                    }
                });
    }

    @FXML
    private void handleDeleteAlbum() {
        Album album = getSelectedAlbum();
        if (album == null) {
            return;
        }
        if (!DialogUtils.confirm("Delete Album", "Delete album \"" + album.getName() + "\"?")) {
            return;
        }
        try {
            app.getService().deleteAlbum(app.getCurrentUser(), album.getName());
            app.save();
            refreshAlbums();
        } catch (Exception exception) {
            DialogUtils.showError("Delete Album Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleOpenAlbum() {
        Album album = getSelectedAlbum();
        if (album == null) {
            return;
        }
        try {
            app.showAlbum(album);
        } catch (Exception exception) {
            DialogUtils.showError("Open Album Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        try {
            app.showSearch();
        } catch (Exception exception) {
            DialogUtils.showError("Search Failed", exception.getMessage());
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

    private Album getSelectedAlbum() {
        Album album = albumTable.getSelectionModel().getSelectedItem();
        if (album == null) {
            DialogUtils.showError("No Selection", "Select an album first.");
        }
        return album;
    }

    private void refreshAlbums() {
        albumTable.setItems(FXCollections.observableArrayList(app.getCurrentUser().getAlbums()));
    }
}
