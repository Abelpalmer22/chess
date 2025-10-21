package server;

import Service.ClearService;
import Service.UserService;
import Service.GameService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.Gson;

public class ClearHandler {
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService, UserService userService, GameService gameService) {
        this.clearService = clearService;
        this.userService = userService;
        this.gameService = gameService;
    }

    public void addRoutes(Javalin app) {
        app.delete("/db", this::handleClear);
    }

    private void handleClear(Context ctx) {
        // clear the services' in-memory data
        userService.clear();
        gameService.clearGames();
        ctx.status(200);
        ctx.result("{}");
    }
}
