package client;

import requests.LoginRequest;
import requests.RegisterRequest;

public class PreloginClient implements ClientMode {
    public String prompt() {
        return "[not logged in] >>> ";
    }

    public String eval(String input, ServerFacade server) {
        String[] t = input.trim().split("\\s+");
        if (t.length == 0) {return "";}
        String cmd = t[0].toLowerCase();

        if (cmd.equals("quit")) {return "__QUIT__";}
        if (cmd.equals("help")) {
            return """
                    Commands:
                    register <username> <password> <email>
                    login <username> <password>
                    quit
                    """;
        }
        if (cmd.equals("register")) {
            if (t.length < 4) {return "usage: register <username> <password> <email>";}
            var req = new RegisterRequest(t[1], t[2], t[3]);
            server.register(req);
            return "registered " + t[1];
        }

        if (cmd.equals("login")) {
            if (t.length < 3) {return "usage: login <username> <password>";}
            var req = new LoginRequest(t[1], t[2]);
            var res = server.login(req);
            return "__LOBBY__ " + res.authToken();
        }

        return "unknown command";
    }
}
