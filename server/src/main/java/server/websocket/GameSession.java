package server.websocket;

import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession { // basically the chat room for a specific game

    private final Map<WsContext, String> clients = new ConcurrentHashMap<>();

    public void addClient(WsContext ctx, String username) {
        clients.put(ctx, username);
    }

    public void scrapClient(WsContext ctx) {
        clients.remove(ctx);
    }

    public Map<WsContext, String> getClients() {
        return clients;
    }
}
