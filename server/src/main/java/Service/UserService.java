package Service;
import java.util.*;
import model.UserData;
import model.AuthData;

public class UserService {
    private final Map<String, UserData> user = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();

    public AuthData register(Map<String, Object> userInfo) throws RuntimeException {
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

}
