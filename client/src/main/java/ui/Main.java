package ui;

import client.Repl;

public class Main {
    public static void main(String[] args) {
        String url = "http://localhost:8080";
        if (args.length > 0) url = args[0];
        new Repl(url).run();
    }
}
