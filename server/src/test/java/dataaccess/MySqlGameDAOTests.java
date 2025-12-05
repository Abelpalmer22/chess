package dataaccess;
import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

public class MySqlGameDAOTests {
    static MySqlUserDAO userDAO;
    static MySqlAuthDAO authDAO;
    static MySqlGameDAO gameDAO;

    @BeforeAll
    public static void init() throws DataAccessException {
        // Create DB and tables once before tests
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
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }

    @Test
    public void createGamePositive() throws DataAccessException {
        GameData game = new GameData(1, "w", null, "test", new ChessGame(), false);
        gameDAO.createGame(game);
        GameData found = gameDAO.getGame(1);
        assertEquals("test", found.gameName());
        assertEquals("w", found.whiteUsername());
    }

    @Test
    public void createGameNegative() throws DataAccessException {
        GameData gameData2 = new GameData(1, "flkj", "sdljf", "slh", new ChessGame(), false);
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(gameData2));
    }

    @Test
    public void getGamePositive() throws DataAccessException {
        GameData game = new GameData(1, "j", "k", "r", new ChessGame(), false);
        GameData check = gameDAO.createGame(game);
        assertEquals(check.gameID(), game.gameID());
        assertDoesNotThrow(() -> gameDAO.getGame(1));
    }

    @Test
    public void getGameNegative() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(3));
    }

    @Test
    public void updateGamePositive() throws DataAccessException {
        GameData game1 = new GameData(1, "w", "b", "name", new ChessGame(), false);
        gameDAO.createGame(game1);
        GameData game2 = new GameData(1, "w", "b", "newname", new ChessGame(), false);
        gameDAO.updateGame(game2);
        GameData found = gameDAO.getGame(1);
        assertEquals("w", found.whiteUsername());
        assertEquals("b", found.blackUsername());
        assertEquals("newname", found.gameName());
    }

    @Test
    public void updateGameNegative() throws DataAccessException {
        GameData fakeGame = new GameData(1, "w", "b", "name", new ChessGame(), false);
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(fakeGame));
    }

    @Test
    public void listGamesPositive() throws DataAccessException {
        gameDAO.createGame(new GameData(1, "w", "b", "n", new ChessGame(), false));
        gameDAO.createGame(new GameData(2, "b", "w", "m", new ChessGame(), false));
        Collection<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesNegative() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS game");
        } catch (SQLException e) {
            fail("Failed to drop table for test setup");
        }
        assertThrows(RuntimeException.class,
                () -> gameDAO.listGames(),
                "Expected RuntimeException when listing games without a valid table");
    }

    @Test
    public void clearTest() throws DataAccessException {
        gameDAO.createGame(new GameData(1, "w", "b", "n", new ChessGame(), false));
        gameDAO.clear();
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(1));
    }

}
