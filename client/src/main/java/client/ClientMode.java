package client;

public interface ClientMode {
    String prompt();
    String eval(String input, ServerFacade server);
}

