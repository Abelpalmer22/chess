package dataaccess;
import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;

import javax.xml.crypto.Data;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

public class MySqlUserDAOTests {
    static MySqlUserDAO userDAO;
    static MySqlAuthDAO authDAO;
    static MySqlGameDAO gameDAO;

    @BeforeAll
    public static void init() throws DataAccessException {
        // Create DB and tables once before all tests
        DatabaseManager.createDatabase();
        MySqlUserDAO.createTable();
        MySqlAuthDAO.createTable();
        MySqlGameDAO.createTable();

        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();
        gameDAO = new MySqlGameDAO();
    }

    @BeforeEach
    public void clearDatabase() throws DataAccessException {
        // Wipe all tables before each test to ensure isolation
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }

    @Test
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("some user", "mypassword", "email");
        userDAO.createUser(user);
        UserData found = userDAO.getUser("some user");
        assertEquals("some user", found.username());
        assertEquals("email", found.email());
    }

    @Test
    void createUserNegative() throws DataAccessException {
        UserData user = new UserData("duplicate", "pawword", "emmail");
        userDAO.createUser(user);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(user), "expected duplicate insert");
    }

    @Test
    void getUserPositive() throws DataAccessException {
        UserData user = new UserData("newuser", "newpassword", "newemail");
        userDAO.createUser(user);
        UserData found = userDAO.getUser("newuser");
        assertNotNull(found);
        assertEquals("newuser", found.username());
        assertEquals("newpassword", found.password());
        assertEquals("newemail", found.email());
    }

    @Test
    void getUserNegative() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> userDAO.getUser("matrix"), "Hey, that user " +
                "isn't supposed to exist");
    }

    @Test
    void clearUserPositive() throws DataAccessException {
        userDAO.createUser(new UserData("i", "o", "u"));
        userDAO.clear();
        assertThrows(DataAccessException.class, () -> userDAO.getUser("i"));
    }

}
