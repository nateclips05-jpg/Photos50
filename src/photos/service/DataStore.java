package photos.service;

import photos.model.AppState;
import photos.model.Album;
import photos.model.Photo;
import photos.model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Handles reading and writing the serialized data file.
 * It is also responsible for creating the default app state the first time the
 * program runs, including the admin user and the stock user.
 *
 * @author Owner
 */
public final class DataStore implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String STATE_FILE = "app-state.ser";

    private final Path dataDirectory;

    /**
     * Creates a data store for a project data directory.
     *
     * @param dataDirectory root data directory
     */
    public DataStore(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public AppState load() throws IOException, ClassNotFoundException {
        Files.createDirectories(dataDirectory);
        Path stateFile = dataDirectory.resolve(STATE_FILE);
        if (!Files.exists(stateFile)) {
            AppState initialState = createInitialState();
            save(initialState);
            return initialState;
        }
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(stateFile))) {
            return (AppState) input.readObject();
        }
    }

    public void save(AppState state) throws IOException {
        Files.createDirectories(dataDirectory);
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(dataDirectory.resolve(STATE_FILE)))) {
            output.writeObject(state);
        }
    }

    private AppState createInitialState() throws IOException {
        AppState state = new AppState();
        state.addUser(new User("admin"));

        User stock = new User("stock");
        Album stockAlbum = new Album("stock");

        for (Path image : findStockImages()) {
            LocalDateTime captureDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(Files.getLastModifiedTime(image).toMillis()),
                    ZoneId.systemDefault()
            ).truncatedTo(ChronoUnit.SECONDS);
            Photo photo = stock.putPhoto(new Photo(image.toAbsolutePath().toString(), captureDate));
            photo.setCaption(image.getFileName().toString());
            stockAlbum.addPhoto(photo);
        }

        stock.addAlbum(stockAlbum);
        state.addUser(stock);
        return state;
    }

    private List<Path> findStockImages() throws IOException {
        Path stockDir = dataDirectory.resolve("stock");
        Files.createDirectories(stockDir);
        try (var stream = Files.list(stockDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".png")
                                || name.endsWith(".jpg")
                                || name.endsWith(".jpeg")
                                || name.endsWith(".gif")
                                || name.endsWith(".bmp");
                    })
                    .sorted()
                    .toList();
        }
    }
}
