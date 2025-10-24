package dataaccess;
import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    Map<String, AuthData> authTokens = new HashMap<>();

    public AuthData createAuth(String username) {
        String authToken = java.util.UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        authTokens.put(authToken, auth);
        return auth;
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        if (authTokens.get(authToken) == null) throw new DataAccessException("unauthorized");
        return authTokens.get(authToken);
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        if (!authTokens.containsKey(authToken)) throw new DataAccessException("unauthorized");
        authTokens.remove(authToken);
    }

    public void clear() {
        authTokens.clear();
    }
}
