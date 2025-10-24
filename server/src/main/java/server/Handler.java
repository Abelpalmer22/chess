package server;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.util.log.Log;
import service.ClearService;
import io.javalin.http.Context;
import com.google.gson.Gson;
import Requests.*;
import Results.*;
import service.GameService;
import service.UserService;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Map;

public class Handler {
    private final ClearService clearService;
    private final Gson serializer = new Gson();
    private final UserService userService;
    private final GameService gameService;

    public Handler(ClearService clearService, UserService userService, GameService gameService) {
        this.clearService = clearService;
        this.userService = userService;
        this.gameService = gameService;
    }

    private void handleError(Context ctx, Exception e) {
        int status = 500; // default
        String message = e.getMessage();

        if (e instanceof DataAccessException exc) {
            // list all the specs' statuses here
            if (message.contains("bad request")) status = 400;
            else if (message.contains("unauthorized")) status = 401;
            else if (message.contains("already taken")) status = 403;
            else if (message.contains("forbidden")) status = 403;
        }

        ctx.status(status);
        ctx.json(Map.of("message", "Error: " + message));
    }


    public void clear(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500);
            ctx.result("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }

    public void register(Context ctx) {
        try {
            RegisterRequest req = serializer.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResult result = userService.register(req);
            ctx.status(200).json(result);
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequest req = serializer.fromJson(ctx.body(), LoginRequest.class);
            LoginResult result = userService.login(req);
            ctx.status(200).json(result);
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    public void logout(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            LogoutRequest req = new LogoutRequest(authToken);
            userService.logout(req);
            ctx.status(200);
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    public void listGames(Context cts) {
        try {
            String authToken = cts.header("Authorization");
            ListGamesRequest req = new ListGamesRequest(authToken);
            ListGamesResult result = gameService.listGames(req);
            cts.status(200).json(result);
        } catch (DataAccessException e) {
            handleError(cts, e);
        }
    }

    public void createGame(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            CreateGameRequest req = serializer.fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResult result = gameService.createGame(req, authToken);
            ctx.status(200).json(result);
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    public void joinGame(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            JoinGameRequest req = serializer.fromJson(ctx.body(), JoinGameRequest.class);
            JoinGameResult result = gameService.joinGame(req, authToken);
            ctx.status(200).json(result);
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }
}






