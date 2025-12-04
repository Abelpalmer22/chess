package client.websocket;

import client.ClientState;
import client.InGameClient;
import com.google.gson.Gson;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;

import jakarta.websocket.*;
import java.net.URI;

import websocket.messages.LoadGameMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;

@ClientEndpoint
public class WSClient {

    private Session session;
    private final ClientState state;
    private final Gson gson = new Gson();

    public WSClient(ClientState state) {
        this.state = state;
    }

    public void connect() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI("ws://localhost:8080/ws"));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        state.setWsSession(session);
        state.setWsClient(this);
        System.out.println("WebSocket connection opened.");
    }


    @OnMessage
    public void onMessage(String json) {

        ServerMessage base = gson.fromJson(json, ServerMessage.class);

        ServerMessageType type = base.getServerMessageType();

        switch (type) {

            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);

                state.setGame(msg.game);

                if (state.getMode() instanceof InGameClient inGame) {
                    inGame.redraw();
                } else {
                    System.out.println("Received LOAD_GAME outside game mode?");
                }
            }

            case ERROR -> {
                ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);
                System.out.println("Error: " + msg.errorMessage);
            }

            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(json, NotificationMessage.class);
                System.out.println(msg.message);
            }

            default -> {
                System.out.println("Unknown server message type: " + type);
            }

        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    public void send(UserGameCommand cmd) {
        try {
            String json = gson.toJson(cmd);
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            System.out.println("Failed to send WS message: " + e.getMessage());
        }
    }
}
