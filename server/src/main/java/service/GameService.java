package service;

import dataaccess.*;
import model.*;
import Requests.*;
import Results.*;
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
        // validate token
        authDAO.getAuth(r.authToken());
        Collection<GameData> games = gameDAO.listGames();
        return new ListGamesResult(games);
    }

    public CreateGameResult createGame(CreateGameRequest r, String authToken) throws DataAccessException {
        if (r == null || r.gameName() == null) {
            throw new DataAccessException("bad request");
        }
        authDAO.getAuth(authToken);
        int id = gameDAO.makeNewID();
        GameData game = new GameData(id, null, null, r.gameName(), new ChessGame());
        gameDAO.createGame(game);
        return new CreateGameResult(id);
    }


    public JoinGameResult joinGame(JoinGameRequest r, String authToken) throws DataAccessException {
        if (r == null) throw new DataAccessException("bad request");
        var auth = authDAO.getAuth(authToken);
        String username = auth.username();
        if (r.playerColor() == null) throw new DataAccessException("bad request");
        GameData game;
        try {
            game = gameDAO.getGame(r.gameID());
        } catch (DataAccessException e) {
            throw new DataAccessException("bad request");
        }
        String color = r.playerColor().trim().toUpperCase();
        if ("WHITE".equals(color)) {
            if (game.whiteUsername() != null) throw new DataAccessException("already taken");
            game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if ("BLACK".equals(color)) {
            if (game.blackUsername() != null) throw new DataAccessException("already taken");
            game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        } else {
            throw new DataAccessException("bad request");
        }

        gameDAO.updateGame(game);
        return new JoinGameResult();
    }

}
