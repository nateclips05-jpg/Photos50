package photos.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Represents the full saved state of the application.
 * This object is what gets serialized to disk so users and their data can be
 * restored the next time the program starts.
 *
 * @author Owner
 */
public final class AppState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<String, User> users = new LinkedHashMap<>();

    public Collection<User> getUsers() {
        return users.values();
    }

    public boolean hasUser(String username) {
        return users.containsKey(User.normalizeUsername(username));
    }

    public User getUser(String username) {
        return users.get(User.normalizeUsername(username));
    }

    public void addUser(User user) {
        String key = User.normalizeUsername(user.getUsername());
        if (users.containsKey(key)) {
            throw new IllegalArgumentException("User already exists.");
        }
        users.put(key, user);
    }

    public User removeUser(String username) {
        return users.remove(User.normalizeUsername(username));
    }
}
