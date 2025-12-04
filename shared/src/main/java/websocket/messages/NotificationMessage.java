package websocket.messages;

public class NotificationMessage extends ServerMessage {
    public String message;

    public NotificationMessage() {
        super(ServerMessageType.NOTIFICATION);
    }
}
