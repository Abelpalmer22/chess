package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import websocket.commands.*;
import websocket.messages.*;

import io.javalin.websocket.WsContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class WSEndpoint {

    private static final Gson GSON = new Gson();
    private static final Map<Integer, GameSession> sessions = new ConcurrentHashMap<>();
    private final GameDAO gameDAO = new MySqlGameDAO();
    private final AuthDAO authDAO = new MySqlAuthDAO();

    public void onClose(WsContext ctx) {
        for (var entry : sessions.entrySet()) {
            entry.getValue().removeClient(ctx);
        }
    }

    public void onMessage(WsContext ctx, String message) {
        try {
            UserGameCommand cmd = GSON.fromJson(message, UserGameCommand.class);
            if (cmd == null || cmd.getCommandType() == null) {
                return;
            }
            switch (cmd.getCommandType()) {
                case CONNECT -> handleConnect(ctx, cmd);
                case MAKE_MOVE -> handleMakeMove(ctx, message, cmd);
                case LEAVE   -> handleLeave(ctx, cmd);
                case RESIGN  -> handleResign(ctx, cmd);
            }
        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void handleConnect(WsContext ctx, UserGameCommand cmd) {
        try {
            var auth = authDAO.getAuthentication(cmd.getAuthToken());
            String username = auth.username();

            GameData game = gameDAO.getGame(cmd.getGameID());
            if (game.gameOver()) {
                sendError(ctx, "This game is over. No new players or observers may join.");
                return;
            }

            GameSession gameSession =
                    sessions.computeIfAbsent(cmd.getGameID(), id -> new GameSession());

            gameSession.addClient(ctx, username);
            LoadGameMessage load = new LoadGameMessage();
            load.game = game.game();
            ctx.send(GSON.toJson(load));
            broadcastExcept(cmd.getGameID(),
                    username + " connected to the game.",
                    ctx);

        } catch (DataAccessException e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void handleMakeMove(WsContext ctx, String json, UserGameCommand baseCmd) {
        try {
            MakeMoveCommand moveCmd = GSON.fromJson(json, MakeMoveCommand.class);
            ChessMove move = moveCmd.move;

            if (move == null) {
                sendError(ctx, "Invalid move format.");
                return;
            }

            var auth = authDAO.getAuthentication(moveCmd.getAuthToken());
            String username = auth.username();
            GameData game = gameDAO.getGame(moveCmd.getGameID());
            if (game.gameOver()) {
                sendError(ctx, "The game is over. No more moves can be made.");
                return;
            }

            ChessGame chess = game.game();
            ChessGame.TeamColor playerColor;
            if (username.equals(game.whiteUsername())) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (username.equals(game.blackUsername())) {
                playerColor = ChessGame.TeamColor.BLACK;
            } else {
                sendError(ctx, "You are not a player in this game.");
                return;
            }
            if (chess.getTeamTurn() != playerColor) {
                sendError(ctx, "It's not your turn.");
                return;
            }

            try {
                chess.makeMove(move);
            } catch (InvalidMoveException ex) {
                sendError(ctx, "That move is not legal.");
                return;
            }

            boolean newGameOver = game.gameOver();

            if (chess.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                broadcast(moveCmd.getGameID(), "White is in checkmate. Game over.");
                newGameOver = true;
            }

            if (chess.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                broadcast(moveCmd.getGameID(), "Black is in checkmate. Game over.");
                newGameOver = true;
            }

            GameData updated = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName(),
                    chess,
                    newGameOver
            );

            gameDAO.updateGame(updated);

            broadcastLoadGame(moveCmd.getGameID(), chess);

            broadcastExcept(
                    moveCmd.getGameID(),
                    username + " made move: " + move,
                    ctx
            );
            if (!newGameOver) {
                if (chess.isInCheck(ChessGame.TeamColor.BLACK)) {
                    broadcast(moveCmd.getGameID(), "Black is in check.");
                }
                if (chess.isInCheck(ChessGame.TeamColor.WHITE)) {
                    broadcast(moveCmd.getGameID(), "White is in check.");
                }
            }

        } catch (DataAccessException e) {
            sendError(ctx, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(ctx, "Unexpected error while making move.");
        }
    }

    private void handleLeave(WsContext ctx, UserGameCommand cmd) {
        try {
            var auth = authDAO.getAuthentication(cmd.getAuthToken());
            String username = auth.username();
            GameData game = gameDAO.getGame(cmd.getGameID());
            boolean different = false;
            if (username.equals(game.whiteUsername())) {
                game = new GameData(
                        game.gameID(),
                        null,
                        game.blackUsername(),
                        game.gameName(),
                        game.game(),
                        game.gameOver()
                );
                different = true;
            }

            else if (username.equals(game.blackUsername())) {
                game = new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        null,
                        game.gameName(),
                        game.game(),
                        game.gameOver()
                );
                different = true;
            }

            if (different) {
                gameDAO.updateGame(game);
            }

            GameSession gameSession = sessions.get(cmd.getGameID());
            if (gameSession != null) {
                gameSession.removeClient(ctx);
            }

            broadcastExcept(
                    cmd.getGameID(),
                    username + " left the game.",
                    ctx
            );

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void handleResign(WsContext ctx, UserGameCommand cmd) {
        try {
            var auth = authDAO.getAuthentication(cmd.getAuthToken());
            String username = auth.username();
            GameData game = gameDAO.getGame(cmd.getGameID());
            boolean isWhite = username.equals(game.whiteUsername());
            boolean isBlack = username.equals(game.blackUsername());

            if (!isWhite && !isBlack) {
                sendError(ctx, "Observers cannot resign.");
                return;
            }
            if (game.gameOver()) {
                sendError(ctx, "The game is already over.");
                return;
            }

            GameData updated = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game(),
                    true
            );

            gameDAO.updateGame(updated);
            broadcast(cmd.getGameID(), username + " resigned. The game is over.");

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void sendError(WsContext ctx, String msg) {
        ErrorMessage err = new ErrorMessage();
        err.errorMessage = msg;
        ctx.send(GSON.toJson(err));
    }

    private void broadcast(int gameID, String msg) {
        NotificationMessage note = new NotificationMessage();
        note.message = msg;

        String json = GSON.toJson(note);

        var gameSession = sessions.get(gameID);
        if (gameSession == null) {
            return;
        }

        for (var entry : gameSession.getClients().entrySet()) {
            entry.getKey().send(json);
        }
    }


    private void broadcastExcept(int gameID, String msg, WsContext exclude) {
        NotificationMessage note = new NotificationMessage();
        note.message = msg;

        String json = GSON.toJson(note);

        var gameSession = sessions.get(gameID);
        if (gameSession == null) {
            return;
        }

        for (var entry : gameSession.getClients().entrySet()) {
            if (!entry.getKey().equals(exclude)) {
                entry.getKey().send(json);
            }
        }
    }


    private void broadcastLoadGame(int gameID, ChessGame game) {
        LoadGameMessage msg = new LoadGameMessage();
        msg.game = game;

        String json = GSON.toJson(msg);

        var gameSession = sessions.get(gameID);
        if (gameSession == null) {
            return;
        }

        for (var entry : gameSession.getClients().entrySet()) {
            entry.getKey().send(json);
        }
    }

}
