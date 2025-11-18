package client;

import chess.ChessGame;

public class InGameClient implements ClientMode {
    private final ClientState state;
    private final boolean observer;

    public InGameClient(ClientState state, boolean observer) {
        this.state = state;
        this.observer = observer;
    }

    private String redraw() {
        boolean whitePerspective = true;
        String color = state.getPlayerColor();
        if ("BLACK".equalsIgnoreCase(color)) {
            whitePerspective = false;
        }
        return DrawBoard.draw(state.getGame(), whitePerspective);
    }

    @Override
    public String prompt() {
        return "[game " + state.getCurrentGameId() + "] >>> ";
    }

    @Override
    public String eval(String input, ServerFacade server) {
        String[] t = input.trim().split("\\s+");
        if (t.length == 0) {return "";}

        String cmd = t[0].toLowerCase();

        if (cmd.equals("help")) {return """
                Commands:
                leave
                resign
                redraw
                quit
                """;}

        if (cmd.equals("quit")) {return "__QUIT__";}

        if (cmd.equals("leave")) {
            return "__LOBBY__";
        }

        if (cmd.equals("resign")) {
            if (observer) {return "You are an observer to this game.";}
            return "resigned";
        }

        if (cmd.equals("redraw")) {
            return redraw();
        }

        return "unknown command";
    }
}
