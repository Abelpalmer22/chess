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
        GameData game = new GameData(id, null, null, r.gameName(), new ChessGame(), false);
        gameDAO.createGame(game);
        return new CreateGameResult(id);
    }

    public JoinGameResult joinGame(JoinGameRequest r, String authToken) throws DataAccessException {
        if (r == null) {
            throw new DataAccessException("bad request");
        }

        var auth = authDAO.getAuthentication(authToken);
        String username = auth.username();

        GameData game = gameDAO.getGame(r.gameID());
        if (game.gameOver()) {
            throw new DataAccessException("forbidden");
        }

        if (r.playerColor() == null) {
            boolean changed = false;

            if (username.equals(game.whiteUsername())) {
                game = new GameData(
                        game.gameID(),
                        null,
                        game.blackUsername(),
                        game.gameName(),
                        game.game(),
                        game.gameOver()
                );
                changed = true;
            }

            if (username.equals(game.blackUsername())) {
                game = new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        null,
                        game.gameName(),
                        game.game(),
                        game.gameOver()
                );
                changed = true;
            }

            if (changed) gameDAO.updateGame(game);
            return new JoinGameResult();
        }

        String color = r.playerColor().trim().toUpperCase();
        if (color.isEmpty()) {
            throw new DataAccessException("bad request");
        }

        if ("WHITE".equals(color)) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("already taken");
            }
            game = new GameData(
                    game.gameID(),
                    username,
                    game.blackUsername(),
                    game.gameName(),
                    game.game(),
                    game.gameOver()
            );
        }
        else if ("BLACK".equals(color)) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("already taken");
            }
            game = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    username,
                    game.gameName(),
                    game.game(),
                    game.gameOver()
            );
        }
        else {
            throw new DataAccessException("bad request");
        }

        gameDAO.updateGame(game);
        return new JoinGameResult();
    }
}
