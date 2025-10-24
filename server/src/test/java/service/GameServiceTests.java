package service;

import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.ListGamesRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GameServiceTests {
    MemoryGameDAO gameDAO = new MemoryGameDAO();
    MemoryAuthDAO authDAO = new MemoryAuthDAO();
    GameService gameService = new GameService(gameDAO, authDAO);

    @Test
    void listTestPositive() throws DataAccessException {
        AuthData tmpAuth = authDAO.createAuthentication("tmpusername");
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
        AuthData auth = authDAO.createAuthentication("someusername");
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
        AuthData authwhite = authDAO.createAuthentication("someusernameidk");
        ChessGame ourGame = new ChessGame();
        GameData game = new GameData(1, null, null, "carlson", ourGame);
        gameDAO.createGame(game);
        AuthData authblack = authDAO.createAuthentication("black");
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
        AuthData authwhite = authDAO.createAuthentication("someusernameidk");
        AuthData authblack = authDAO.createAuthentication(null);
        ChessGame ourGame = new ChessGame();
        GameData game = new GameData(1, null, null, "carlson", ourGame);
        gameDAO.createGame(game);
        JoinGameRequest reqwhite = new JoinGameRequest("WHITE", game.gameID());
        JoinGameRequest reqblack = new JoinGameRequest(null, game.gameID());
        Assertions.assertThrows(DataAccessException.class, () -> gameService.joinGame(reqblack, authblack.authToken()));
    }
}
