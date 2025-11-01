package dataaccess;

import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.Map;

public interface UserDAO {
    public void createUser(UserData user) throws DataAccessException;
    public UserData getUser(String username) throws DataAccessException;
    public void clear() throws DataAccessException;
}
