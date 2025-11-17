package client;

public class InGameClient implements ClientMode {
    private final String authToken;
    private final int gameID;
    private final boolean observer;

    public InGameClient(String authToken, int gameID) {
        this(authToken, gameID, false);
    }

    public InGameClient(String authToken, int gameID, boolean observer) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.observer = observer;

    }

    public String prompt() {
        return "[game " + gameID + "] >>> ";
    }

    public String eval(String input, ServerFacade server) {
        String[] t = input.trim().split("\\s+");
        if (t.length == 0) return "";

        String cmd = t[0].toLowerCase();

        if (cmd.equals("help")) return """
                Commands:
                leave
                resign
                redraw
                quit
                """;

        if (cmd.equals("quit")) return "__QUIT__";

        if (cmd.equals("leave")) return "__LOBBY__";

        if (cmd.equals("resign")) return "resigned";

        if (cmd.equals("redraw")) return "board not implemented";

        return "unknown command";
    }
}
