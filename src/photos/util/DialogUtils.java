package photos.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

import java.util.List;
import java.util.Optional;

/**
 * Small helper class for common JavaFX dialogs.
 * Keeping these methods here avoids repeating alert and input dialog setup in
 * every controller.
 *
 * @author Owner
 */
public final class DialogUtils {
    private DialogUtils() {
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(title);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static Optional<String> promptText(String title, String prompt, String initialValue) {
        TextInputDialog dialog = new TextInputDialog(initialValue == null ? "" : initialValue);
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText(prompt);
        return dialog.showAndWait().map(String::trim).filter(text -> !text.isEmpty());
    }

    public static Optional<String> promptTextAllowBlank(String title, String prompt, String initialValue) {
        TextInputDialog dialog = new TextInputDialog(initialValue == null ? "" : initialValue);
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText(prompt);
        return dialog.showAndWait().map(String::trim);
    }

    public static <T> Optional<T> promptChoice(String title, String prompt, List<T> choices) {
        if (choices.isEmpty()) {
            return Optional.empty();
        }
        ChoiceDialog<T> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText(prompt);
        return dialog.showAndWait();
    }
}
