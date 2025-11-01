package server;
import io.javalin.Javalin;
import dataaccess.*;
import io.javalin.json.JavalinGson;
import service.ClearService;
import service.GameService;
import service.UserService;

import javax.xml.crypto.Data;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });

        try {
            DatabaseManager.createDatabase();
            MySqlUserDAO.createTable();
            MySqlAuthDAO.createTable();
            MySqlGameDAO.createTable();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        var userDAO = new MySqlUserDAO();
        var gameDAO = new MySqlGameDAO();
        var authDAO = new MySqlAuthDAO();

        // Register your endpoints and exception handlers here.
        ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);
        UserService userService = new UserService(gameDAO, authDAO, userDAO);
        GameService gameService = new GameService(gameDAO, authDAO);
        Handler handler = new Handler(clearService, userService, gameService);

        javalin.delete("/db", handler::clear);
        javalin.post("/user", handler::register);
        javalin.post("/session", handler::login);
        javalin.delete("/session", handler::logout);
        javalin.get("/game", handler::listGames);
        javalin.post("/game", handler::createGame);
        javalin.put("/game", handler::joinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}








