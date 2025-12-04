package client;

import client.websocket.WSClient;
import jakarta.websocket.Session;
import chess.ChessGame;

public class ClientState {

    private WSClient wsClient;
    private Session wsSession;
    private ChessGame game;
    private String authToken;
    private Integer currentGameId;
    private String playerColor;
    private ClientMode mode;

    public WSClient getWsClient() { return wsClient; }
    public void setWsClient(WSClient wsClient) { this.wsClient = wsClient; }

    public Session getWsSession() { return wsSession; }
    public void setWsSession(Session session) { this.wsSession = session; }

    public ChessGame getGame() { return game; }
    public void setGame(ChessGame game) { this.game = game; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String token) { this.authToken = token; }

    public Integer getCurrentGameId() { return currentGameId; }
    public void setCurrentGameId(Integer id) { this.currentGameId = id; }

    public String getPlayerColor() { return playerColor; }
    public void setPlayerColor(String color) { this.playerColor = color; }

    public ClientMode getMode() { return mode; }
    public void setMode(ClientMode mode) { this.mode = mode; }
}
