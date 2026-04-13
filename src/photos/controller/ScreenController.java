package photos.controller;

import photos.Photos;

import java.io.IOException;

/**
 * Shared interface for controllers that need access to the main application.
 * It keeps screen setup consistent whenever a new scene is shown.
 *
 * @author Owner
 */
public interface ScreenController {
    void setApp(Photos app);

    default void onShow() throws IOException {
    }
}
