package me.sailex.secondbrain.player2;

import com.google.gson.*;
import lombok.Getter;
import me.sailex.secondbrain.exception.LLMServiceException;
import me.sailex.secondbrain.player2.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This class acts as a client for interacting with the Player2 API.
 */
public class Player2APIClient {

    private static final String BASE_URL = "http://127.0.0.1:4315";
    private final Gson gson;
    private final HttpClient client;

    public Player2APIClient() {
        this.gson = new Gson();
        this.client = HttpClient.newHttpClient();
    }

    @Getter
    private enum API_ENDPOINT {
        SELECT_CHARACTERS("/v1/selected_characters"),
        TTS_START("/v1/tts/speak"),
        STT_START("/v1/stt/start"),
        STT_STOP("/v1/stt/stop"),
        HEALTH("/v1/health");

        private final String url;

        API_ENDPOINT(String url) {
            this.url = url;
        }
    }

    /**
     * Gets Characters selected in Player2 APP.
     */
    public Characters getSelectedCharacters() throws LLMServiceException {
        try {
            return sendGetRequest(API_ENDPOINT.SELECT_CHARACTERS.getUrl(), Characters.class);
        } catch (Exception e) {
            throw new LLMServiceException("Failed to fetch selected characters", e);
        }
    }

    /**
     * Initiates a text-to-speech process for the provided message using the specified voice IDs.
     */
    public String startTextToSpeech(String message, List<String> voiceIds) throws LLMServiceException {
        try {
            String url = API_ENDPOINT.TTS_START.getUrl();
            TTSSpeakRequest speakRequest = new TTSSpeakRequest(message, voiceIds);
            TTSSpeakResponse speakResponse = sendPostRequest(url, speakRequest, TTSSpeakResponse.class);
            return speakResponse.data();
        } catch (Exception e) {
            throw new LLMServiceException("Failed to start text to speech for message: " + message, e);
        }
    }

    /**
     * Initiates a speech-to-text process with the specified timeout.
     */
    public void startSpeechToText(int timeout) {
        try {
            String url = API_ENDPOINT.STT_START.getUrl();

            JsonObject sttStartRequestBody = new JsonObject();
            sttStartRequestBody.addProperty("timeout", timeout);

            sendPostRequest(url, sttStartRequestBody, JsonObject.class);
        } catch (Exception e) {
            throw new LLMServiceException("Failed to start retrieving text from speech input", e);
        }
    }

    /**
     * Stops the ongoing speech-to-text process and retrieves the resulting text from the service.
     */
    public String stopSpeechToText() throws LLMServiceException {
        try {
            String url = API_ENDPOINT.STT_START.getUrl();
            STTResponse speechToTextResponse = sendPostRequest(url, new JsonObject(), STTResponse.class);
            return speechToTextResponse.text();
        } catch (Exception e) {
            throw new LLMServiceException("Failed to retrieve text from speech input", e);
        }
    }

    /**
     * Sends a heart-beat request to the service.
     */
    public HealthResponse getHealthStatus() throws LLMServiceException {
        try {
            String url = API_ENDPOINT.HEALTH.getUrl();
            return sendGetRequest(url, HealthResponse.class);
        } catch(Exception e) {
            throw new LLMServiceException("Failed to send health check", e);
        }
    }

    private <T> T sendPostRequest(String url, Object requestBody, Class<T> responseType) throws IOException {
        String requestJson = gson.toJson(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();
        return sendRequest(request, responseType);
    }

    private <T> T sendGetRequest(String url, Class<T> responseType) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + url))
                .GET()
                .build();
        return sendRequest(request, responseType);
    }

    private <T> T sendRequest(HttpRequest request, Class<T> responseType) throws IOException {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status != 200) {
                throw new IOException(status + " - " + response.uri() + " responseBody: " + response.body());
            }
            return gson.fromJson(response.body(), responseType);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

}