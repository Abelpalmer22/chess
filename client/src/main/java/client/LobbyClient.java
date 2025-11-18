package client;

import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;

public class LobbyClient implements ClientMode {
    private final ClientState state;

    public LobbyClient(ClientState state) {
        this.state = state;
    }

    public String prompt() {
        return "[lobby] >>> ";
    }

    public String eval(String input, ServerFacade server) {
        String[] t = input.trim().split("\\s+");
        if (t.length == 0) return "";

        String cmd = t[0].toLowerCase();
        String authToken = state.getAuthToken(); // ← ALWAYS use shared state

        if (cmd.equals("help")) return """
                Commands:
                list
                create
                play
                observe
                logout
                quit
                """;

        if (cmd.equals("quit")) return "__QUIT__";

        if (cmd.equals("logout")) {
            server.logout(state.getAuthToken());
            state.setAuthToken(null);
            return "__LOGGED_OUT__";
        }

        if (cmd.equals("list")) {
            var res = server.listGames(authToken);
            StringBuilder gamesList = new StringBuilder();
            for (GameData game: res.games()) {
                gamesList.append(game.gameName())
                        .append(" ")
                        .append(game.whiteUsername())
                        .append(" ")
                        .append(game.blackUsername())
                        .append("\n");
            }
            return gamesList.toString();
        }

        if (cmd.equals("create")) {
            if (t.length < 2) return "format: create <gameName>";
            var req = new CreateGameRequest(t[1]);
            var res = server.createGame(req, authToken);
            return "created game " + res.gameID();
        }

        if (cmd.equals("play")) {
            if (t.length < 3) return "format: play <gameID> <WHITE|BLACK>";
            int id = Integer.parseInt(t[1]);
            var req = new JoinGameRequest(t[2], id);
            server.joinGame(req, authToken);

            state.setCurrentGameId(id);
            state.setPlayerColor(t[2].toUpperCase());
            state.setGame(new chess.ChessGame());

            return "__GAME__";
        }

        if (cmd.equals("observe")) {
            if (t.length < 2) return "format: observe <gameID>";
            int id = Integer.parseInt(t[1]);
            var req = new JoinGameRequest(null, id);
            server.joinGame(req, authToken);

            state.setCurrentGameId(id);
            state.setPlayerColor(null);
            state.setGame(new chess.ChessGame());

            return "__OBSERVE__";
        }

        return "unknown command";
    }
}
