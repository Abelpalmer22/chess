package dataaccess;

import model.AuthData;
import model.UserData;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    public UserData createUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null) {
            throw new DataAccessException("Benutzer lasst sich nicht Null sein");
        }
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Benutzer existiert schon");
        }
        users.put(user.username(), user);
        return user;
    }

    public UserData getUser(String username) throws DataAccessException {
        if (username == null) {
            throw new DataAccessException("Benutzernamen lasst sich nicht Null sein");
        }
        UserData user = users.get(username);
        if (user == null) {throw new DataAccessException("Benutzer nicht gefunden");}
        return user;
    }

    public void clear() {
        users.clear();
    }
}
