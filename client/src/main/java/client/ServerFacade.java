package client;
import com.google.gson.Gson;
import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import results.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final String baseUrl;
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();

    public ServerFacade(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RegisterResult register(RegisterRequest req) throws RuntimeException {
        return send("POST", "/user", req, null, RegisterResult.class);
    }

    public LoginResult login(LoginRequest req) throws RuntimeException {
        return send("POST", "/session", req, null, LoginResult.class);
    }

    public void logout(String authToken) throws RuntimeException {
        send("DELETE", "/session", null, authToken, Void.class);
    }

    public ListGamesResult listGames(String authToken) throws RuntimeException {
        return send("GET", "/game", null, authToken, ListGamesResult.class);
    }

    public CreateGameResult createGame(CreateGameRequest req, String authToken) throws RuntimeException {
        return send("POST", "/game", req, authToken, CreateGameResult.class);
    }

    public JoinGameResult joinGame(JoinGameRequest req, String authToken) throws RuntimeException {
        return send("PUT", "/game", req, authToken, JoinGameResult.class);
    }

    public GameData getGame(int gameID, String token) {
        return send("GET", "/game/" + gameID, null, token, GameData.class);
    }


    private <T> T send(String method, String path, Object body, String token, Class<T> type) {
        var url = baseUrl + path;
        var builder = HttpRequest.newBuilder().uri(URI.create(url));

        if (token != null) builder.header("Authorization", token);
        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(gson.toJson(body)));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpResponse<String> response;
        try {
            response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("connection failed");
        }

        var code = response.statusCode();
        var json = response.body();

        if (code == 200) {
            if (type == Void.class) return null;
            return gson.fromJson(json, type);
        }

        if (json.contains("already taken")) throw new RuntimeException("already taken");
        if (json.contains("unauthorized")) throw new RuntimeException("unauthorized");
        if (json.contains("forbidden")) throw new RuntimeException("forbidden");
        if (json.contains("bad request")) throw new RuntimeException("bad request");




        throw new RuntimeException("request failed");
    }

}
