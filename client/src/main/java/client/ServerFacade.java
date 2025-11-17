package client;
import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final String baseUrl;
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();

    public ServerFacade(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
