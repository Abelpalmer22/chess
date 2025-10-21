package Service;

import java.util.*;
import model.UserData;
import model.AuthData;

public class UserService {
    private final Map<String, UserData> user = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();

    public AuthData register(Map<String, String> userInfo) throws RuntimeException {
        String username = (String) userInfo.get("username");
        String password = (String) userInfo.get("password");
        String email = (String) userInfo.get("email");
        if (username == null || password == null || email == null) throw new RuntimeException("Invalid credentials");
        if (user.containsKey(username)) throw new RuntimeException("User already exists");
        UserData newUser = new UserData(username, password, email);
        user.put(username, newUser);
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        authTokens.put(authToken, auth);
        return auth;
    }

    // login: check username/password and return new token
    public AuthData login(Map<String, String> loginInfo) {
        String username = (String) loginInfo.get("username");
        String password = (String) loginInfo.get("password");
        if (username == null || password == null) throw new RuntimeException("Invalid credentials");
        UserData stored = user.get(username);
        if (stored == null || !stored.password().equals(password)) {
            throw new RuntimeException("Invalid credentials");
        }
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        authTokens.put(authToken, auth);
        return auth;
    }

    public void logout(String authToken) {
        authTokens.remove(authToken);
    }

    public AuthData getAuthFromToken(String token) {
        if (token == null) return null;
        return authTokens.get(token);
    }

    public void clear() {
        user.clear();
        authTokens.clear();
    }
}

