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
        if (t.length == 0) {return "";}

        String cmd = t[0].toLowerCase();
        String authToken = state.getAuthToken();

        switch (cmd) {
            case "help" -> {
                return """
                        Commands:
                        list
                        create
                        play
                        observe
                        logout
                        quit
                        """;
            }
            case "quit" -> {
                return "__QUIT__";
            }
            case "logout" -> {
                server.logout(state.getAuthToken());
                state.setAuthToken(null);
                return "__LOGGED_OUT__";
            }
            case "list" -> {
                var res = server.listGames(authToken);
                StringBuilder gamesList = new StringBuilder();
                int i = 0;
                for (GameData game : res.games()) {
                    gamesList.append("Game Number: ")
                            .append(i + 1)
                            .append("\n  Game Name: ")
                            .append(game.gameName())
                            .append(game.gameOver() ? "\n  <<THIS GAME IS OVER>>" : "")
                            .append("\n  White Player: ")
                            .append(game.whiteUsername() != null ? game.whiteUsername() : "")
                            .append("\n  Black Player: ")
                            .append(game.blackUsername() != null ? game.blackUsername() : "")
                            .append("\n");
                    i++;
                }
                return gamesList.toString();
            }
            case "create" -> {
                if (t.length < 2) {
                    return "format: create <gameName>";
                }
                var req = new CreateGameRequest(t[1]);
                var res = server.createGame(req, authToken);
                return "created game " + res.gameID();
            }
            case "play" -> {
                if (t.length < 3) {return "format: play <gameID> <WHITE|BLACK>";}

                int id = Integer.parseInt(t[1]);
                var req = new JoinGameRequest(t[2], id);
                server.joinGame(req, authToken);

                return "__GAME__ " + authToken + " " + id + " " + t[2].toUpperCase();
            }
            case "observe" -> {
                if (t.length < 2) {
                    return "format: observe <gameID>";
                }
                int id = Integer.parseInt(t[1]);
                var req = new JoinGameRequest(null, id);
                server.joinGame(req, authToken);

                state.setCurrentGameId(id);
                state.setPlayerColor(null);
                state.setGame(new chess.ChessGame());

                return "__OBSERVE__";
            }
        }
        return "unknown command";
    }
}
