package client;

import org.junit.jupiter.api.*;
import requests.RegisterRequest;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    private static String baseURL;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        baseURL = "http://localhost:" + port;
        System.out.println("Started test HTTP server on port " + port);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

}
