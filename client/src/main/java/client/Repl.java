package client;
import java.util.Scanner;

public class Repl {
    private final ServerFacade server;
    private ClientMode mode = new PreloginClient();
    private final Scanner scanner = new Scanner(System.in);

    public Repl(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to the chess app. Please login to continue.");

    }
}
