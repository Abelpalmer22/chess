package client;

import java.util.Scanner;

public class Repl {
    private final ServerFacade server;
    private final ClientState state = new ClientState();
    private ClientMode mode = new PreloginClient(state);
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
            //make a bunch of if statmeents for each possible choice
            if (result.equals("__QUIT__")) return;
            if (result.equals("__LOGGED_OUT__")) {
                mode = new PreloginClient(state);
                continue;
            }
            if (result.equals("__LOBBY__")) {
                mode = new LobbyClient(state);
                continue;
            }

            if (result.equals("__GAME__")) {
                mode = new InGameClient(state, false);
                continue;
            }

            if (result.equals("__OBSERVE__")) {
                mode = new InGameClient(state, true);
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
