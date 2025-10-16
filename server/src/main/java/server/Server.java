package server;

import io.javalin.*;

public class Server {

    private final Javalin javalin;
    private final UserService userService;

    public Server() {
        userService = new UserService;
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("db", ctx -> ctx.result("{}"));
        javalin.post("user", this::register);

        // Register your endpoints and exception handlers here.

    }

    private void register(Context ctx) {
        var serializer = new Gson();
        String reqJson = ctx.body();
        var user = serializer.fromJson(reqJson, Map.class);

        var authData = userService.register(user)


        ctx.result(serializer.toJson(authData));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
