package client;

import chess.ChessGame;

public class ClientState {
    private String authToken;
    private Integer currentGameId;
    private String playerColor;
    private ChessGame game;

    public String getAuthToken() {
        return authToken;
    }
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public Integer getCurrentGameId() {
        return currentGameId;
    }
    public void setCurrentGameId(Integer id) {
        this.currentGameId = id;
    }

    public String getPlayerColor() {
        return playerColor;
    }
    public void setPlayerColor(String color) {
        this.playerColor = color;
    }

    public ChessGame getGame() {
        return game;
    }
    public void setGame(ChessGame game) {
        this.game = game;
    }
}
