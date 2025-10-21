package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO {
    private final Map<String, AuthData> usersByToken = new HashMap<>();
    private final Map<String, AuthData> usersByUsername = new HashMap<>();

    public void createUser(String usenrame, String authToken) {
        if (usersByUsername.containsKey(usenrame)) {
            throw new RuntimeException("User already exists");
        }
        AuthData authData = new AuthData(authToken, usenrame);
        usersByUsername.put(usenrame, authData);
        usersByToken.put(authToken, authData);
    }

    public AuthData getUserByUsername(String username) {
        return usersByUsername.get(username);
    }

    public AuthData getUserByToken(String authToken) {
        return usersByToken.get(authToken);
    }

    public void clear() {
        usersByToken.clear();
        usersByUsername.clear();
    }
}
