package photos.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import photos.Photos;
import photos.model.User;
import photos.util.DialogUtils;

/**
 * Handles the login screen.
 * This controller reads the username, tries to sign the user in, and also
 * lets the user close the application from the first screen.
 *
 * @author Owner
 */
public final class LoginController implements ScreenController {
    @FXML
    private TextField usernameField;

    private Photos app;

    @Override
    public void setApp(Photos app) {
        this.app = app;
    }

    @Override
    public void onShow() {
        usernameField.clear();
        usernameField.requestFocus();
    }

    @FXML
    private void handleLogin() {
        try {
            User user = app.getService().login(usernameField.getText());
            app.login(user);
        } catch (Exception exception) {
            DialogUtils.showError("Login Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleQuit() {
        try {
            app.save();
            Platform.exit();
        } catch (Exception exception) {
            DialogUtils.showError("Save Error", exception.getMessage());
        }
    }
}
