package me.sailex.secondbrain.llm.player2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import lombok.Getter;
import me.sailex.secondbrain.exception.LLMServiceException;
import me.sailex.secondbrain.history.Message;
import me.sailex.secondbrain.history.MessageConverter;
import me.sailex.secondbrain.llm.ALLMClient;
import me.sailex.secondbrain.llm.function_calling.FunctionProvider;
import me.sailex.secondbrain.llm.player2.model.ChatMessage;
import me.sailex.secondbrain.llm.roles.ChatRole;
import me.sailex.secondbrain.llm.player2.model.*;
import me.sailex.secondbrain.util.LogUtil;
import org.apache.http.HttpException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

/**
 * This class acts as a client for interacting with the Player2 API.
 */
public class Player2APIClient extends ALLMClient<FunctionDef> {

    private static final String BASE_URL = "http://127.0.0.1:4315";
    private static final int MAX_TOOL_CALL_RETRIES = 1;

    private final String voiceId;
    private final String npcName; //only for debugging
    private final ObjectMapper mapper;
    private final HttpClient client;
    private final FunctionExecutor functionExecutor;

    public Player2APIClient() {
        this(null, null, "default", 10);
    }

    public Player2APIClient(FunctionProvider<FunctionDef> functionManager, String voiceId, String npcName, int timeout) {
        super(functionManager);
        this.voiceId = voiceId;
        this.npcName = npcName;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
        this.functionExecutor = new FunctionExecutor();
    }

    @Getter
    private enum API_ENDPOINT {
        CHAT_COMPLETION("/v1/chat/completions"),
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
     * Executes functions that the LLM called based on the prompt and registered functions.
     */
    @Override
    public Message callFunctions(List<Message> messages) throws LLMServiceException {
        try {
            functionExecutor.enrollFunctions(functionManager.getFunctions());
            ChatRequest request = ChatRequest.builder()
                    .tools(functionExecutor.getToolFunctions())
                    .messages(messages.stream()
                            .map(MessageConverter::toChatMessage)
                            .toList())
                    .build();

            ResponseMessage result = sendChatRequest(request);
            executeFunction(result.tool_calls().getFirst());

            return MessageConverter.toMessage(result);
        } catch (Exception e) {
            throw new LLMServiceException("Could not call functions for prompt: " + messages.getLast(), e);
        }
    }

    private ResponseMessage sendChatRequest(ChatRequest request) throws IOException, HttpException {
        return sendPostRequest(
                API_ENDPOINT.CHAT_COMPLETION.getUrl(),
                request,
                Chat.class
        ).firstMessage();
    }

    private void executeFunction(ToolCall toolCall) {
        FunctionCall function = toolCall.function();
        String functionName = function.getName();
        String arguments = function.getArguments();

        String result = functionExecutor.execute(function);

        String toolResult = functionName + "(" + arguments + ") : " + result;
        LogUtil.info(toolResult);
    }

    @Override
    public Message chat(Message messages) {
        return null;
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
    public String startTextToSpeech(String message) throws LLMServiceException {
        try {
            String url = API_ENDPOINT.TTS_START.getUrl();
            TTSSpeakRequest speakRequest = new TTSSpeakRequest(message, List.of(voiceId));
            TTSSpeakResponse speakResponse = sendPostRequest(url, speakRequest, TTSSpeakResponse.class);
            return speakResponse.data();
        } catch (Exception e) {
            throw new LLMServiceException("Failed to start text to speech for message: " + message, e);
        }
    }

    public void startSpeechToText() {
        startSpeechToText(30);
    }

    /**
     * Initiates a speech-to-text process with the specified timeout.
     */
    public void startSpeechToText(double timeout) {
        try {
            String url = API_ENDPOINT.STT_START.getUrl();

            ObjectNode sttStartRequestBody = mapper.createObjectNode();
            sttStartRequestBody.put("timeout", timeout);

            sendPostRequest(url, sttStartRequestBody, ObjectNode.class);
        } catch (Exception e) {
            throw new LLMServiceException("Failed to start retrieving text from speech input", e);
        }
    }

    /**
     * Stops the ongoing speech-to-text process and retrieves the resulting text from the service.
     */
    public String stopSpeechToText() throws LLMServiceException {
        try {
            String url = API_ENDPOINT.STT_STOP.getUrl();
            STTResponse speechToTextResponse = sendPostRequest(url, mapper.createObjectNode(), STTResponse.class);
            String text = speechToTextResponse.text();
            if (text.isBlank()) {
                throw new LLMServiceException("We couldn't understand you. Maybe your microphone is muted.");
            }
            return text;
        } catch (IOException | HttpException e) {
            throw new LLMServiceException("Failed to retrieve text from speech input", e);
        }
    }
    /**
     * Sends a heart-beat request to the service.
     */
    public HealthResponse getHealthStatus() throws LLMServiceException {
        try {
            String url = API_ENDPOINT.HEALTH.getUrl();
            return sendGetRequest(url, HealthResponse.class, "player2-game-key", MOD_ID);
        } catch(HttpException e) {
            throw new LLMServiceException("Failed to send health check", e);
        } catch (IOException e) {
            throw new LLMServiceException("Player2 API is not reachable", e);
        }
    }

    @Override
    public void checkServiceIsReachable() throws LLMServiceException {
        throw new UnsupportedOperationException("dont use this. this is checked by getHealthStatus");
    }

    private <T> T sendPostRequest(String url, Object requestBody, Class<T> responseType) throws IOException, HttpException {
        String requestJson = mapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();
        LogUtil.info(npcName + " - " + request.method() + " - " + request.uri() + ": " + requestJson);
        return sendRequest(request, responseType);
    }

    private <T> T sendGetRequest(String url, Class<T> responseType, String... headers) throws IOException, HttpException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + url))
                .GET();
        if (headers.length > 0) {
            builder.headers(headers);
        }
        HttpRequest request = builder.build();
        LogUtil.info(npcName + " - " + request.method() + " - " + request.uri() + " headers: " + Arrays.toString(headers));
        return sendRequest(request, responseType);
    }

    private <T> T sendRequest(HttpRequest request, Class<T> responseType) throws IOException, HttpException {
        HttpResponse<String> response = sendRequest(request);
        LogUtil.info(npcName + " - " + response.statusCode() + " - " + response.uri() + ": " + mapper.writeValueAsString(response.body()));
        return mapper.readValue(response.body(), responseType);
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, HttpException {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status != 200) {
                throw new HttpException(status + " - " + response.uri() + " responseBody: " + response.body());
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

}