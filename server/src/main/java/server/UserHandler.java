package server;

import Service.UserService;
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.AuthData;
import Requests.RegisterRequest;
import Requests.LoginRequest;
import org.eclipse.jetty.server.Authentication;

import java.util.Map;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void addRoutes(Javalin app) {
        app.post("/user", this::handleRegister);
        app.post("/session", this::handleLogin);
        app.delete("/session", this::handleLogout);
    }

    private void handleRegister(Context ctx) {
        RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);
        var map = Map.of(
                "username", req.username(),
                "password", req.password(),
                "email", req.email()
        );

        AuthData auth = userService.register(map);
        ctx.status(200);
        ctx.result(gson.toJson(auth));
    }

    private void handleLogin(Context ctx) {
        LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
        var map = Map.of("username", req.username(), "password", req.password());
        AuthData auth = userService.login(map);
        ctx.status(200);
        ctx.result(gson.toJson(auth));
    }

    private void handleLogout(Context ctx) {
        String authToken = ctx.header("authorization");
        if (authToken == null) {
            ctx.status(400);
            ctx.result("{}");
            return;
        }
        userService.logout(authToken);
        ctx.status(200);
        ctx.result("{}");
    }
}
