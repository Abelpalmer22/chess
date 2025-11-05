package dataaccess;
import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;

import javax.xml.crypto.Data;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

public class MySqlAuthDAOTests {
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
    public void createAuthPositive() throws DataAccessException {
        AuthData auth = authDAO.createAuthentication("sbel");
        AuthData check = authDAO.getAuthentication(auth.authToken());
        assertEquals("sbel", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    public void createAuthNegative() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> authDAO.createAuthentication(null));
        AuthData auth = authDAO.createAuthentication("username");
        assertDoesNotThrow(() -> authDAO.createAuthentication("username"));
    }

    @Test
    public void getAuthPositive() throws DataAccessException {
        AuthData auth = authDAO.createAuthentication("sbel");
        AuthData check = authDAO.getAuthentication(auth.authToken());
        assertNotNull(check);
        assertEquals(auth.authToken(), check.authToken());
        assertEquals(auth.username(), check.username());
    }

    @Test
    public void getAuthNegative() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> authDAO.getAuthentication("random"), "shouldn't work");
    }

    @Test
    public void deleteAuthPositive() throws DataAccessException {
        AuthData auth = authDAO.createAuthentication("some");
        authDAO.deleteAuthentication(auth.authToken());
        assertThrows(DataAccessException.class, () -> authDAO.getAuthentication(auth.authToken()));
    }

    @Test
    public void deleteAuthNegative() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuthentication("sometoken"),
                "hey that shouldnt exist");
    }

    @Test
    public void clearTest() throws DataAccessException {
        AuthData auth = authDAO.createAuthentication("a;slfj");
        authDAO.clear();
        assertThrows(DataAccessException.class, () -> authDAO.getAuthentication(auth.authToken()));
    }


}