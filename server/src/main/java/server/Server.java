package server;

import Service.ClearService;
import Service.GameService;
import Service.UserService;
import io.javalin.*;
import com.google.gson.Gson;
import io.javalin.http.Context;
import org.eclipse.jetty.server.Authentication;

import java.util.Map;


public class Server {

    private final Javalin javalin;
    private final UserHandler userHandler;
    private final GameHandler gameHandler;
    private final ClearHandler clearHandler;

    public Server() {
        var userService = new UserService();
        var gameService = new GameService();
        var clearService = new ClearService();

        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService, userService);
        clearHandler = new ClearHandler(clearService, userService, gameService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        userHandler.addRoutes(javalin);
        gameHandler.addRoutes(javalin);
        clearHandler.addRoutes(javalin);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void clearDatabase(Context ctx) {
        ctx.status(200);
        ctx.result("{}");
    }
}
