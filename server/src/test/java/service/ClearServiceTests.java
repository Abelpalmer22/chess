package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClearServiceTests {
    MemoryGameDAO gameDAO = new MemoryGameDAO();
    MemoryUserDAO userDAO = new MemoryUserDAO();
    MemoryAuthDAO authDAO = new MemoryAuthDAO();
    ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

    @Test
    void clearTest() throws DataAccessException {
        clearService.clear();
        userDAO.createUser(new UserData("abelpalmer22", "latterdaysaint", "abelpalmer22@gmail.com"));
        GameData fakeGame = gameDAO.createGame(new GameData(1, "abel", "jesse", "wurgie", new ChessGame()));
        AuthData fakeAuth = authDAO.createAuthentication("abelpalmer22");
        clearService.clear();
        Assertions.assertThrows(DataAccessException.class, () -> gameDAO.getGame(1));
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.getAuthentication(fakeAuth.authToken()));
        Assertions.assertThrows(DataAccessException.class, () -> userDAO.getUser("abelpalmer22"));
    }
}
