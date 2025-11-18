package client;

import requests.LoginRequest;
import requests.RegisterRequest;

public class PreloginClient implements ClientMode {

    private final ClientState state;

    public PreloginClient(ClientState state) {
        this.state = state;
    }

    @Override
    public String prompt() {
        return "[logged out] >>> ";
    }

    @Override
    public String eval(String input, ServerFacade server) {
        String[] t = input.trim().split("\\s+");
        if (t.length == 0) return "";

        String cmd = t[0].toLowerCase();

        if (cmd.equals("help")) {
            return """
                    Commands:
                    register <username> <password> <email>
                    login <username> <password>
                    quit
                    """;
        }
        if (cmd.equals("quit")) {
            return "__QUIT__";
        }
        if (cmd.equals("login")) {
            if (t.length < 3) {
                return "format: login <username> <password>";
            }

            var req = new LoginRequest(t[1], t[2]);
            var res = server.login(req);
            state.setAuthToken(res.authToken());
            return "__LOBBY__";
        }
        if (cmd.equals("register")) {
            if (t.length < 4) {
                return "format: register <username> <password> <email>";
            }

            var req = new RegisterRequest(t[1], t[2], t[3]);
            var res = server.register(req);
            state.setAuthToken(res.authToken());
            return "__LOBBY__";
        }

        return "unknown command";
    }
}

