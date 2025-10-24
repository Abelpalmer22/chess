package service;

import Requests.CreateGameRequest;
import Requests.JoinGameRequest;
import Requests.ListGamesRequest;
import Results.CreateGameResult;
import Results.ListGamesResult;
import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public class GameServiceTests {
    MemoryGameDAO gameDAO = new MemoryGameDAO();
    MemoryAuthDAO authDAO = new MemoryAuthDAO();
    GameService gameService = new GameService(gameDAO, authDAO);

    @Test
    void listTestPositive() throws DataAccessException {
        AuthData tmpAuth = authDAO.createAuth("tmpusername");
        ListGamesRequest lgreq = new ListGamesRequest(tmpAuth.authToken());
        ListGamesResult lgr = gameService.listGames(lgreq);
        Assertions.assertNotNull(tmpAuth);
        Assertions.assertNotNull(lgr.games());
        Assertions.assertTrue(lgr.games().isEmpty());
    }

    @Test
    void listTestNegative() throws DataAccessException {
        ListGamesRequest r = new ListGamesRequest(null);
        Assertions.assertThrows(DataAccessException.class, () -> gameService.listGames(r));
    }

    @Test
    void createPositive() throws DataAccessException {
        AuthData auth = authDAO.createAuth("someusername");
        CreateGameRequest cgr = new CreateGameRequest("someGame");
        Assertions.assertDoesNotThrow(() -> gameService.createGame(cgr, auth.authToken()));
        CreateGameResult res = gameService.createGame(cgr, auth.authToken());
        Assertions.assertNotNull(res);
        Assertions.assertNotNull(gameDAO.getGame(res.gameID()));
    }

    @Test
    void createNegative() throws DataAccessException {
        CreateGameRequest r = null;
        String authToken = null;
        Assertions.assertThrows(DataAccessException.class, () -> gameService.createGame(r, authToken));
        CreateGameRequest req = new CreateGameRequest(null);
        Assertions.assertThrows(DataAccessException.class, () -> gameService.createGame(req, "anyString"));
    }

    @Test
    void joinTestPositive() throws DataAccessException {
        AuthData authwhite = authDAO.createAuth("someusernameidk");
        ChessGame ourGame = new ChessGame();
        GameData game = new GameData(1, null, null, "carlson", ourGame);
        gameDAO.createGame(game);
        AuthData authblack = authDAO.createAuth("black");
        JoinGameRequest reqwhite = new JoinGameRequest("WHITE", game.gameID());
        JoinGameRequest reqblack = new JoinGameRequest("BLACK", game.gameID());
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(reqwhite, authwhite.authToken()));
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(reqblack, authblack.authToken()));

        GameData updated = gameDAO.getGame(game.gameID());
        Assertions.assertEquals("someusernameidk", updated.whiteUsername());
        Assertions.assertEquals("black", updated.blackUsername());
    }

    @Test
    void joinTetNegative() throws DataAccessException {
        JoinGameRequest nullreq = null;
        Assertions.assertThrows(DataAccessException.class, () -> gameService.joinGame(nullreq, "anyString"));
        AuthData authwhite = authDAO.createAuth("someusernameidk");
        AuthData authblack = authDAO.createAuth(null);
        ChessGame ourGame = new ChessGame();
        GameData game = new GameData(1, null, null, "carlson", ourGame);
        gameDAO.createGame(game);
        JoinGameRequest reqwhite = new JoinGameRequest("WHITE", game.gameID());
        JoinGameRequest reqblack = new JoinGameRequest(null, game.gameID());
        Assertions.assertThrows(DataAccessException.class, () -> gameService.joinGame(reqblack, authblack.authToken()));
    }
}
