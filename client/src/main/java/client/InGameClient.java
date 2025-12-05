package client;

public class InGameClient implements ClientMode {
    private final ClientState state;
    private final boolean observer;

    public InGameClient(ClientState state, boolean observer) {
        this.state = state;
        this.observer = observer;
    }


    public String redraw() {
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
        if (t.length == 0) return "";

        String cmd = t[0].toLowerCase();

        if (cmd.equals("help")) {return """
        Gameplay Commands:
        move <start> <end>
        highlight <square>
        draw
        leave
        resign
        quit
        """;}


        if (cmd.equals("quit")) {
            var ws = state.getWsClient();
            var message = new websocket.commands.UserGameCommand(
                    websocket.commands.UserGameCommand.CommandType.LEAVE,
                    state.getAuthToken(),
                    state.getCurrentGameId()
            );
            ws.send(message);
            return "__QUIT__";
        }


        if (cmd.equals("leave")) {
            var ws = state.getWsClient();

            var message = new websocket.commands.UserGameCommand(
                    websocket.commands.UserGameCommand.CommandType.LEAVE,
                    state.getAuthToken(),
                    state.getCurrentGameId()
            );

            ws.send(message);

            return "__LOBBY__";
        }


        if (cmd.equals("resign")) {
            if (observer) {
                return "Observers cannot resign.";
            }

            var ws = state.getWsClient();
            var message = new websocket.commands.UserGameCommand(
                    websocket.commands.UserGameCommand.CommandType.RESIGN,
                    state.getAuthToken(),
                    state.getCurrentGameId()
            );

            ws.send(message);
            return "You resigned.";
        }


        if (cmd.equals("draw")) {
            return redraw();
        }

        if (cmd.equals("move")) {
            if (observer) {
                return "Observers cannot make moves.";
            }
            if (t.length != 3) {
                return "Format: move <start> <end>  Example: move e2 e4";
            }

            try {
                var startPos = DrawBoard.parsePosition(t[1]);
                var endPos   = DrawBoard.parsePosition(t[2]);

                var move = new chess.ChessMove(startPos, endPos, null);

                var send = new websocket.commands.MakeMoveCommand(
                        state.getAuthToken(),
                        state.getCurrentGameId(),
                        move
                );

                state.getWsClient().send(send);
                return "";

            } catch (Exception e) {
                return "Invalid move format.";
            }
        }

        if (cmd.equals("highlight")) {
            if (t.length != 2) {
                return "Format: highlight <position>  Example: highlight e2";
            }
            try {
                var pos = DrawBoard.parsePosition(t[1]);
                return DrawBoard.drawHighlight(state.getGame(), pos,
                        "BLACK".equalsIgnoreCase(state.getPlayerColor()) ? false : true);
            } catch (Exception e) {
                return "Invalid square.";
            }
        }
        return "unknown command";
    }
}

