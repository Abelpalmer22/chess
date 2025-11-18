package client;

import org.junit.jupiter.api.*;
import requests.*;
import results.*;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    private static String baseURL;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        baseURL = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    private ServerFacade facade() {
        return new ServerFacade(baseURL);
    }

    @Test
    public void registerPositive() {
        var f = facade();
        var req = new RegisterRequest("someweirduser", "pass", "u@test.com");
        RegisterResult res = f.register(req);

        Assertions.assertNotNull(res);
        Assertions.assertNotNull(res.authToken());
    }

    @Test
    public void registerNegative() {
        var f = facade();
        var req = new RegisterRequest("someotheruser", "pass", "u@test.com");
        f.register(req); // first time ok

        Assertions.assertThrows(RuntimeException.class, () ->
                f.register(req)
        );
    }

    @Test
    public void loginPositive() {
        var f = facade();
        f.register(new RegisterRequest("hiimauser", "pw", "l@test.com"));

        LoginResult res = f.login(new LoginRequest("hiimauser", "pw"));
        Assertions.assertNotNull(res.authToken());
    }

    @Test
    public void loginNegative() {
        var f = facade();
        f.register(new RegisterRequest("imalsoauser", "correct", "x@test.com"));

        Assertions.assertThrows(RuntimeException.class, () ->
                f.login(new LoginRequest("imalsoauser", "wrong"))
        );
    }

    @Test
    public void logoutPositive() {
        var f = facade();
        var reg = f.register(new RegisterRequest("use", "pw", "x@test.com"));

        Assertions.assertDoesNotThrow(() ->
                f.logout(reg.authToken())
        );
    }

    @Test
    public void logoutNegative() {
        var f = facade();

        Assertions.assertThrows(RuntimeException.class, () ->
                f.logout("token")
        );
    }

    @Test
    public void listGamesPositive() {
        var f = facade();
        var reg = f.register(new RegisterRequest("myfavuser", "pw", "l@test.com"));

        ListGamesResult list = f.listGames(reg.authToken());
        Assertions.assertEquals(0, list.games().size());
    }

    @Test
    public void listGamesNegative() {
        var f = facade();

        Assertions.assertThrows(RuntimeException.class, () ->
                f.listGames("token")
        );
    }

    @Test
    public void createGamePositive() {
        var f = facade();
        var reg = f.register(new RegisterRequest("user", "pw", "x@test.com"));

        CreateGameResult result =
                f.createGame(new CreateGameRequest("game"), reg.authToken());

        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameNegative() {
        var f = facade();

        Assertions.assertThrows(RuntimeException.class, () ->
                f.createGame(new CreateGameRequest("game"), "token")
        );
    }

    @Test
    public void joinGamePositive() {
        var f = facade();
        var reg = f.register(new RegisterRequest("user", "pw", "x@test.com"));

        CreateGameResult game =
                f.createGame(new CreateGameRequest("game"), reg.authToken());

        Assertions.assertDoesNotThrow(() ->
                f.joinGame(new JoinGameRequest("WHITE", game.gameID()), reg.authToken())
        );
    }

    @Test
    public void joinGameNegative() {
        var f = facade();
        var reg = f.register(new RegisterRequest("somefavuser", "pw", "x@test.com"));
        CreateGameResult game =
                f.createGame(new CreateGameRequest("game"), reg.authToken());

        Assertions.assertThrows(RuntimeException.class, () ->
                f.joinGame(new JoinGameRequest("WHITE", game.gameID()), "token")
        );
    }
}
