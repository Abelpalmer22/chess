package client.websocket;
import client.ClientState;
import client.InGameClient;
import com.google.gson.Gson;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;

import org.glassfish.tyrus.client.ClientManager;

import jakarta.websocket.*;
import java.net.URI;
import java.nio.ByteBuffer;


public class WSClient {

    private Session session;
    private final ClientState state;
    private final Gson gson = new Gson();

    public WSClient(ClientState state) {
        this.state = state;
    }

    public void connect() throws Exception {
        ClientManager client = ClientManager.createClient();
        client.connectToServer(new Endpoint() {
            @Override
            public void onOpen(Session session, EndpointConfig config) {
                WSClient.this.session = session;
                state.setWsClient(WSClient.this);
                session.addMessageHandler(String.class, WSClient.this::withConnect);
                new Thread(() -> {
                    while (session != null && session.isOpen()) {
                        try {
                            Thread.sleep(20_000);
                            session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[]{1}));
                        } catch (Exception ignored) {}
                    }
                }).start();

            }
        }, ClientEndpointConfig.Builder.create().build(), new URI("ws://localhost:8080/ws"));
    }

    private void printPrompt() {
        Integer id = state.getCurrentGameId();
        if (id != null) {
            System.out.print("[game " + id + "] >>> ");
        } else {
            System.out.print("> ");
        }
        System.out.flush();
    }

    private void withConnect(String json) {
        ServerMessage base = gson.fromJson(json, ServerMessage.class);

        switch (base.getServerMessageType()) {

            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);
                state.setGame(msg.game);
                InGameClient view = new InGameClient(state, false);

                System.out.println();
                System.out.print(view.redraw());
                printPrompt();
            }

            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(json, NotificationMessage.class);

                System.out.println();
                System.out.println(msg.message);
                printPrompt();
            }

            case ERROR -> {
                ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);
                System.out.println();
                System.out.println("Error: " + msg.errorMessage);
                printPrompt();
            }
        }
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

