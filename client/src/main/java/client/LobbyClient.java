package client;

import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;

public class LobbyClient implements ClientMode {
    private final String authToken;

    public LobbyClient(String authToken) {
        this.authToken = authToken;
    }

    public String prompt() {
        return "[lobby] >>> ";
    }

    public String eval(String input, ServerFacade server) {
        String[] t = input.trim().split("\\s+");
        if (t.length == 0) return "";

        String cmd = t[0].toLowerCase();

        if (cmd.equals("help")) return """
                Commands:
                list
                create
                play
                logout
                quit
                """;

        if (cmd.equals("quit")) return "__QUIT__";

        if (cmd.equals("logout")) return "__LOBBY__";

        if (cmd.equals("list")) {
            var res = server.listGames(authToken);
            String gamesList = "";
            for (GameData game: res.games()) {
                gamesList += game.gameName() + " " + game.whiteUsername() + " " + game.blackUsername() + "\n";
            }
            return gamesList;
        }

        if (cmd.equals("create")) {
            if (t.length < 2) return "usage: create <gameName>";
            var req = new CreateGameRequest(t[1]);
            var res = server.createGame(req, authToken);
            return "created game " + res.gameID();
        }

        if (cmd.equals("play")) {
            if (t.length < 3) return "usage: play <gameID> <WHITE|BLACK>";
            int id = Integer.parseInt(t[1]);
            var req = new JoinGameRequest(t[2], id);
            server.joinGame(req, authToken);
            return "__GAME__ " + authToken + " " + id;
        }

        return "unknown command";
    }
}
