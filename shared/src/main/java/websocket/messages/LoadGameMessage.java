package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {
    public ChessGame game;

    public LoadGameMessage() {
        super(ServerMessageType.LOAD_GAME);
    }
}
