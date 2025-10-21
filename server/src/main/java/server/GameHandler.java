package server;

import Service.GameService;
import Service.UserService;
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.AuthData;
import model.GameData;
import Requests.CreateGameRequest;
import Requests.JoinGameRequest;

import java.util.Map;

public class GameHandler {
    private final GameService gameService;
    private final UserService userService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    public void addRoutes(Javalin app) {
        app.post("/game", this::handleCreateGame);
        app.put("/game/{id}", this::handleJoinGame);
        app.get("/game", this::handleListGames);
    }

    private void handleCreateGame(Context ctx) {
        try {
            CreateGameRequest req = gson.fromJson(ctx.body(), CreateGameRequest.class);
            AuthData auth = userService.getAuthFromToken(ctx.header("authorization"));
            if (auth == null) {
                ctx.status(401).result("{}");
                return;
            }

            Map<String, Object> map = Map.of(
                    "opponent", req.opponent(),
                    "gameName", req.gameName()
            );

            GameData created = gameService.createGame(map, auth);
            ctx.status(200).result(gson.toJson(created));
        } catch (RuntimeException e) {
            ctx.status(400).result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Internal error")));
        }
    }

    private void handleJoinGame(Context ctx) {
        try {
            int gameId = Integer.parseInt(ctx.pathParam("id"));
            AuthData auth = userService.getAuthFromToken(ctx.header("authorization"));
            if (auth == null) {
                ctx.status(401).result("{}");
                return;
            }

            GameData updated = gameService.joinGame(gameId, auth);
            ctx.status(200).result(gson.toJson(updated));
        } catch (NumberFormatException e) {
            ctx.status(400).result(gson.toJson(Map.of("message", "Invalid game id")));
        } catch (RuntimeException e) {
            ctx.status(400).result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Internal error")));
        }
    }

    private void handleListGames(Context ctx) {
        ctx.status(200).result(gson.toJson(Map.of("games", new GameData[0])));
    }
}

