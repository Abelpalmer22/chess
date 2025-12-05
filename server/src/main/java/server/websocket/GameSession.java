package server.websocket;

import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession {

    private final Map<WsContext, String> clients = new ConcurrentHashMap<>();

    public void addClient(WsContext ctx, String username) {
        clients.put(ctx, username);
    }

    public void removeClient(WsContext ctx) {
        clients.remove(ctx);
    }

    public Map<WsContext, String> getClients() {
        return clients;
    }
}
