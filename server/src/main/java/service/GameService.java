package service;

import dataaccess.*;
import model.*;
import requests.*;
import results.*;
import chess.ChessGame;

import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public ListGamesResult listGames(ListGamesRequest r) throws DataAccessException {
        authDAO.getAuthentication(r.authToken());
        Collection<GameData> games = gameDAO.listGames();
        return new ListGamesResult(games);
    }

    public CreateGameResult createGame(CreateGameRequest r, String authToken) throws DataAccessException {
        if (r == null || r.gameName() == null) {
            throw new DataAccessException("bad request");
        }
        authDAO.getAuthentication(authToken);
        int id = gameDAO.makeNewID();
        GameData game = new GameData(id, null, null, r.gameName(), new ChessGame());
        gameDAO.createGame(game);
        return new CreateGameResult(id);
    }


    public JoinGameResult joinGame(JoinGameRequest r, String authToken) throws DataAccessException {
        if (r == null) {
            throw new DataAccessException("bad request");
        }
        var auth = authDAO.getAuthentication(authToken);
        String username = auth.username();
        if (r.playerColor() == null) {
            return new JoinGameResult();
        }
        GameData game;
        game = gameDAO.getGame(r.gameID());
        String color = r.playerColor().trim().toUpperCase();
        if ("WHITE".equals(color)) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("already taken");
            }
            game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if ("BLACK".equals(color)) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("already taken");
            }
            game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        } else {
            throw new DataAccessException("bad request");
        }

        gameDAO.updateGame(game);
        return new JoinGameResult();
    }

}
