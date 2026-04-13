package photos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import photos.controller.ScreenController;
import photos.model.Album;
import photos.model.AppState;
import photos.model.Photo;
import photos.model.User;
import photos.service.DataStore;
import photos.service.PhotoLibraryService;
import photos.util.DialogUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * Main JavaFX application class for the photo manager.
 * This class starts the program, loads saved data, and switches between the
 * login, admin, album, and search screens as the user moves through the app.
 *
 * @author Owner
 */
public final class Photos extends Application {
    private static final double WIDTH = 1200;
    private static final double HEIGHT = 760;

    private Stage primaryStage;
    private PhotoLibraryService service;
    private User currentUser;
    private Album currentAlbum;
    private List<Photo> lastSearchResults = List.of();

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Photos50");

        Path dataDirectory = Path.of(System.getProperty("user.dir"), "data");
        DataStore dataStore = new DataStore(dataDirectory);
        AppState appState = dataStore.load();
        service = new PhotoLibraryService(dataStore, appState);

        primaryStage.setOnCloseRequest(event -> {
            try {
                save();
            } catch (IOException exception) {
                event.consume();
                DialogUtils.showError("Save Error", exception.getMessage());
            }
        });

        showLogin();
        primaryStage.show();
    }

    public PhotoLibraryService getService() {
        return service;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Album getCurrentAlbum() {
        return currentAlbum;
    }

    public List<Photo> getLastSearchResults() {
        return lastSearchResults;
    }

    public void setLastSearchResults(List<Photo> photos) {
        lastSearchResults = List.copyOf(photos);
    }

    public void login(User user) throws IOException {
        currentUser = user;
        currentAlbum = null;
        lastSearchResults = List.of();
        if ("admin".equals(user.getUsername())) {
            showAdmin();
        } else {
            showUserHome();
        }
    }

    public void logout() throws IOException {
        save();
        currentUser = null;
        currentAlbum = null;
        lastSearchResults = List.of();
        showLogin();
    }

    public void save() throws IOException {
        service.save();
    }

    public void showLogin() throws IOException {
        setScene("view/login.fxml");
    }

    public void showAdmin() throws IOException {
        setScene("view/admin.fxml");
    }

    public void showUserHome() throws IOException {
        currentAlbum = null;
        setScene("view/user-home.fxml");
    }

    public void showAlbum(Album album) throws IOException {
        currentAlbum = album;
        setScene("view/album.fxml");
    }

    public void showSearch() throws IOException {
        setScene("view/search.fxml");
    }

    private void setScene(String resourceName) throws IOException {
        URL resource = Photos.class.getResource(resourceName);
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        Object controller = loader.getController();
        if (controller instanceof ScreenController screenController) {
            screenController.setApp(this);
            screenController.onShow();
        }

        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        } else {
            primaryStage.getScene().setRoot(root);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
