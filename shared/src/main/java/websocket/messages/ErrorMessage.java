package websocket.messages;

public class ErrorMessage extends ServerMessage {
    public String errorMessage;

    public ErrorMessage() {
        super(ServerMessageType.ERROR);
    }
}
