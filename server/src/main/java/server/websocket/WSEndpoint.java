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
    private static final Map<Integer, GameSession> SESSIONS = new ConcurrentHashMap<>();
    private final GameDAO gameDAO = new MySqlGameDAO();
    private final AuthDAO authDAO = new MySqlAuthDAO();

    public void withClose(WsContext ctx) {
        for (var entry : SESSIONS.entrySet()) {
            entry.getValue().scrapClient(ctx);
        }
    }

    public void withMessage(WsContext ctx, String message) {
        try {
            UserGameCommand cmd = GSON.fromJson(message, UserGameCommand.class);
            if (cmd == null || cmd.getCommandType() == null) {return;}

            switch (cmd.getCommandType()) {
                case CONNECT -> handleConnect(ctx, cmd);
                case MAKE_MOVE -> handleMakeMove(ctx, message, cmd);
                case LEAVE     -> handleLeave(ctx, cmd);
                case RESIGN    -> handleResign(ctx, cmd);
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
                    SESSIONS.computeIfAbsent(cmd.getGameID(), id -> new GameSession());

            gameSession.addClient(ctx, username);

            LoadGameMessage load = new LoadGameMessage();
            load.game = game.game();
            ctx.send(GSON.toJson(load));

            sendMessage(cmd.getGameID(),
                    username + " connected to the game.",
                    ctx);

        } catch (DataAccessException e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void handleMakeMove(WsContext ctx, String json, UserGameCommand cmd) {
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
            }
            else if (username.equals(game.blackUsername())) {
                playerColor = ChessGame.TeamColor.BLACK;
            }
            else {
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
                sendError(ctx, "Illegal move");
                return;
            }

            boolean newGameOver = game.gameOver();
            String whiteUser = game.whiteUsername();
            String blackUser = game.blackUsername();

            if (chess.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                sendMessage(moveCmd.getGameID(), whiteUser + " is in checkmate. Game over.", null);
                newGameOver = true;
            }
            if (chess.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                sendMessage(moveCmd.getGameID(), blackUser + "is in checkmate. Game over.", null);
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

            sendMessage(
                    moveCmd.getGameID(),
                    username + " made move: " + move,
                    ctx
            );

            if (!newGameOver) {
                if (chess.isInCheck(ChessGame.TeamColor.BLACK)) {
                    sendMessage(moveCmd.getGameID(), blackUser + " is in check.", null);
                }
                if (chess.isInCheck(ChessGame.TeamColor.WHITE)) {
                    sendMessage(moveCmd.getGameID(), whiteUser + " is in check.", null);
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
            boolean changed = false;

            if (username.equals(game.whiteUsername())) {
                game = new GameData(
                        game.gameID(), null, game.blackUsername(),
                        game.gameName(), game.game(), game.gameOver()
                );
                changed = true;
            }
            else if (username.equals(game.blackUsername())) {
                game = new GameData(
                        game.gameID(), game.whiteUsername(), null,
                        game.gameName(), game.game(), game.gameOver()
                );
                changed = true;
            }

            if (changed) {gameDAO.updateGame(game);}

            GameSession gameSession = SESSIONS.get(cmd.getGameID());
            if (gameSession != null) {
                gameSession.scrapClient(ctx);
            }

            sendMessage(cmd.getGameID(),
                    username + " left the game.",
                    ctx);

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

            sendMessage(cmd.getGameID(),
                    username + " resigned. The game is over.",
                    null);

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    private void sendError(WsContext ctx, String msg) {
        ErrorMessage err = new ErrorMessage();
        err.errorMessage = msg;
        ctx.send(GSON.toJson(err));
    }

    private void sendMessage(int gameID, String msg, WsContext exclude) {
        NotificationMessage note = new NotificationMessage();
        note.message = msg;

        String json = GSON.toJson(note);
        var gameSession = SESSIONS.get(gameID);
        if (gameSession == null) {return;}

        for (var entry : gameSession.getClients().entrySet()) {
            WsContext client = entry.getKey();
            if (exclude != null && client.equals(exclude)) {continue;}
            client.send(json);
        }
    }

    private void broadcastLoadGame(int gameID, ChessGame game) {
        LoadGameMessage msg = new LoadGameMessage();
        msg.game = game;
        String json = GSON.toJson(msg);
        var gameSession = SESSIONS.get(gameID);
        if (gameSession == null) {return;}

        for (var entry : gameSession.getClients().entrySet()) {
            entry.getKey().send(json);
        }
    }

}
