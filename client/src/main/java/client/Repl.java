package client;

import java.util.Scanner;

public class Repl {
    private final ServerFacade server;
    private ClientMode mode = new PreloginClient();
    private final Scanner scanner = new Scanner(System.in);

    public Repl(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to the chess app. Please login to continue");

        while (true) {
            System.out.print(mode.prompt());
            String input = scanner.nextLine();

            String result;
            try {
                result = mode.eval(input, server);
            } catch (Exception e) {
                System.out.println("Error: " + clean(e.getMessage()));
                continue;
            }

            if (result.equals("__QUIT__")) return;
            if (result.startsWith("__LOBBY__")) {
                String token = result.substring("__LOBBY__".length()).trim();
                mode = new LobbyClient(token);
                continue;
            }
            if (result.startsWith("__GAME__")) {
                String[] parts = result.substring("__GAME__".length()).trim().split(" ");
                String token = parts[0];
                int id = Integer.parseInt(parts[1]);
                mode = new InGameClient(token, id);
                continue;
            }

            if (result.startsWith("__OBSERVE__")) {
                var parts = result.split("\\s+");
                var token = parts[1];
                var gameID = Integer.parseInt(parts[2]);
                mode = new InGameClient(token, gameID, true); // true = observer mode
                continue;
            }

            System.out.println(result);
        }
    }

    private String clean(String m) {
        if (m == null) return "unknown error";
        if (m.toLowerCase().contains("unauthorized")) return "unauthorized";
        if (m.toLowerCase().contains("already taken")) return "already taken";
        if (m.toLowerCase().contains("forbidden")) return "forbidden";
        if (m.toLowerCase().contains("bad request")) return "bad request";
        return "request failed";
    }
}
