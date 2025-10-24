package dataaccess;
import model.*;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException {
        if (user.username() == null) throw new DataAccessException("Username null");
        if (user.password() == null) throw new DataAccessException("Password null");
        if (users.get(user.username()) != null) throw new DataAccessException("User already exists");
        users.put(user.username(), user);
    }

    public UserData getUser(String username) throws DataAccessException {
        if (username == null) throw new DataAccessException("bad request");
        if (users.get(username) == null) throw new DataAccessException("User not found");
        return users.get(username);
    }

    public Map<String, UserData> getUsers() {
        return users;
    }

    public void clear() {
        users.clear();
    }
}