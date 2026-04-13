package photos.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import photos.Photos;
import photos.model.User;
import photos.util.DialogUtils;

/**
 * Handles the admin subsystem.
 * The admin screen is only for listing users and creating or deleting them.
 *
 * @author Owner
 */
public final class AdminController implements ScreenController {
    @FXML
    private ListView<User> userListView;

    private Photos app;

    @Override
    public void setApp(Photos app) {
        this.app = app;
    }

    @Override
    public void onShow() {
        refreshUsers();
    }

    @FXML
    private void handleCreateUser() {
        DialogUtils.promptText("Create User", "Username:", "")
                .ifPresent(username -> {
                    try {
                        app.getService().createUser(username);
                        app.save();
                        refreshUsers();
                    } catch (Exception exception) {
                        DialogUtils.showError("Create User Failed", exception.getMessage());
                    }
                });
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.showError("No Selection", "Select a user to delete.");
            return;
        }
        if (!DialogUtils.confirm("Delete User", "Delete user \"" + selected.getUsername() + "\"?")) {
            return;
        }
        try {
            app.getService().deleteUser(selected.getUsername());
            app.save();
            refreshUsers();
        } catch (Exception exception) {
            DialogUtils.showError("Delete User Failed", exception.getMessage());
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

    private void refreshUsers() {
        userListView.setItems(FXCollections.observableArrayList(app.getService().listUsers()));
    }
}
