package client;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessMove;

public class InGameClient implements ClientMode {
    private final ClientState state;
    private final boolean observer;
    private boolean waitingForResignConfirm = false;

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
        if (waitingForResignConfirm) {
            String ans = input.trim().toLowerCase();

            if (ans.equals("yes")) {
                waitingForResignConfirm = false;

                var ws = state.getWsClient();
                var message = new websocket.commands.UserGameCommand(
                        websocket.commands.UserGameCommand.CommandType.RESIGN,
                        state.getAuthToken(),
                        state.getCurrentGameId()
                );
                ws.send(message);

                return "You resigned.";
            }

            if (ans.equals("no")) {
                waitingForResignConfirm = false;
                return "Resignation canceled.";
            }

            return "Please type yes or no:";
        }

        String[] t = input.trim().split("\\s+");
        if (t.length == 0) {return "";}

        String cmd = t[0].toLowerCase();

        if (cmd.equals("help")) {
            return """
                    Gameplay Commands:
                    move <start> <end> [promotion]
                      e.g. move e7 e8 q
                    highlight <square>
                    draw
                    leave
                    resign
                    quit
                    """;
        }

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

            waitingForResignConfirm = true;
            return "Are you sure you want to resign? (yes/no)";
        }

        if (cmd.equals("draw")) {
            return redraw();
        }

        if (cmd.equals("move")) {
            if (observer) {
                return "Observers cannot make moves.";
            }

            if (t.length < 3 || t.length > 4) {
                return "Format: move <start> <end> [promotion]\n" +
                        "Example: move e7 e8 q";
            }

            try {
                var startPos = DrawBoard.parsePosition(t[1]);
                var endPos   = DrawBoard.parsePosition(t[2]);

                ChessPiece.PieceType promotion = null;

                var board = state.getGame().getBoard();
                var movingPiece = board.getPiece(startPos);

                if (movingPiece != null &&
                        movingPiece.getPieceType() == ChessPiece.PieceType.PAWN) {

                    int targetRow = endPos.getRow();
                    if (targetRow == 1 || targetRow == 8) {

                        if (t.length == 4) {
                            promotion = parsePromotionToken(t[3]);
                            if (promotion == null) {
                                return "Invalid promotion piece. Use q, r, b, n or names.";
                            }
                        } else {
                            promotion = ChessPiece.PieceType.QUEEN;
                        }
                    }
                }

                var move = new ChessMove(startPos, endPos, promotion);
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
                return DrawBoard.drawHighlight(
                        state.getGame(),
                        pos,
                        !"BLACK".equalsIgnoreCase(state.getPlayerColor())
                );
            } catch (Exception e) {
                return "Invalid square.";
            }
        }

        return "unknown command";
    }
    private ChessPiece.PieceType parsePromotionToken(String token) {
        token = token.toLowerCase();
        switch (token) {
            case "q", "queen" -> {
                return ChessPiece.PieceType.QUEEN;
            }
            case "r", "rook" -> {
                return ChessPiece.PieceType.ROOK;
            }
            case "b", "bishop" -> {
                return ChessPiece.PieceType.BISHOP;
            }
            case "n", "knight" -> {
                return ChessPiece.PieceType.KNIGHT;
            }
            default -> {
                return null;
            }
        }
    }
}

