package client.websocket;

import client.ClientMode;
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
import org.glassfish.tyrus.client.ClientManager;
import java.net.URI;


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
                System.out.println("WebSocket connected.");

                session.addMessageHandler(String.class, WSClient.this::onMessage);

                new Thread(() -> {
                    while (WSClient.this.session != null && WSClient.this.session.isOpen()) {
                        try {
                            Thread.sleep(25_000);
                            WSClient.this.session.getAsyncRemote().sendText("{\"type\":\"PING\"}");
                        } catch (Exception ignored) {}
                    }
                }).start();
            }

            @Override
            public void onClose(Session session, CloseReason closeReason) {
                System.out.println("WebSocket closed: " + closeReason);
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

    private void onMessage(String json) {
        ServerMessage base = gson.fromJson(json, ServerMessage.class);

        switch (base.getServerMessageType()) {

            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);
                state.setGame(msg.game);

                // Always redraw the board from current state
                InGameClient view = new InGameClient(state, false);

                System.out.println();              // break away from any partial prompt
                System.out.print(view.redraw());   // draw the board
                printPrompt();                     // show [game X] >>> again
            }

            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(json, NotificationMessage.class);

                System.out.println();              // new line after any prompt
                System.out.println(msg.message);   // print notification
                printPrompt();                     // reprint prompt
            }

            case ERROR -> {
                ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);

                System.out.println();                                  // new line after prompt
                System.out.println("Error: " + msg.errorMessage);      // print error
                printPrompt();                                         // reprint prompt
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

