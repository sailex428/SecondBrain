package me.sailex.secondbrain.mineskin;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MineSkinProxyClient {

    public static final String PROXY_URL = "https://mineskin.sailex.me/skin";
    public static final Gson GSON = new Gson();

    private final HttpClient client;

    public MineSkinProxyClient() {
        client = HttpClient.newHttpClient();
    }

    public SkinResponse getSkin(String url) {
        try {
            SkinRequest requestBody = new SkinRequest(url, Variant.AUTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PROXY_URL))
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(requestBody)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return GSON.fromJson(response.body(), SkinResponse.class);
        } catch (IOException | InterruptedException | JsonParseException e) {
            throw new MineSkinProxyClientException("Failed to get skin for: " + url, e);
        }
    }

}
