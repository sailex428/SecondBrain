package me.sailex.secondbrain.llm.player2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.Getter;
import me.sailex.secondbrain.exception.LLMServiceException;
import me.sailex.secondbrain.history.ConversationHistory;
import me.sailex.secondbrain.llm.ALLMClient;
import me.sailex.secondbrain.llm.player2.model.*;
import me.sailex.secondbrain.model.function_calling.FunctionResponse;
import me.sailex.secondbrain.util.LogUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.sailex.secondbrain.SecondBrain.MOD_ID;

/**
 * This class acts as a client for interacting with the Player2 API.
 */
public class Player2APIClient extends ALLMClient<FunctionDef> {

    private static final String BASE_URL = "http://127.0.0.1:4315";
    private static final int MAX_TOOL_CALL_RETRIES = 4;

    private final List<String> voiceIds;
    private final String npcName; //only for debugging
    private final ObjectMapper mapper;
    private final HttpClient client;
    private final FunctionExecutor functionExecutor;

    public Player2APIClient() {
        this(new ArrayList<>(), "default");
    }

    public Player2APIClient(List<String> voiceIds, String npcName) {
        this.voiceIds = voiceIds;
        this.npcName = npcName;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.client = HttpClient.newHttpClient();
        this.functionExecutor = new FunctionExecutor();
    }

    @Getter
    private enum API_ENDPOINT {
        CHAT_COMPLETION("/v1/chat/completions"),
        SELECT_CHARACTERS("/v1/selected_characters"),
        TTS_START("/v1/tts/speak"),
        STT_START("/v1/stt/start"),
        STT_STOP("/v1/stt/stop"),
        HEALTH("/v1/health"),
        PING("/docs");

        private final String url;

        API_ENDPOINT(String url) {
            this.url = url;
        }
    }

    /**
     * Executes functions that the LLM called based on the prompt and registered functions.
     *
     * @param 	prompt 	   the user prompt
     * @param 	functions  functions that the llm is allowed to call
     * @return             the formatted results of the function calls.
     */
    @Override
    public FunctionResponse callFunctions(
        String prompt,
        List<FunctionDef> functions,
        ConversationHistory conversationHistory
    ) throws LLMServiceException {
        try {
            functionExecutor.enrollFunctions(functions);
            StringBuilder calledFunctions = new StringBuilder();
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.UserMessage.of(conversationHistory.getFormattedHistory()));
            messages.add(ChatMessage.UserMessage.of(prompt));

            ChatMessage.ResponseMessage result = sendChatRequest(messages);
            List<ToolCall> toolCalls = result.getToolCalls();

            for (int toolCallTries = 0; toolCalls != null && !toolCalls.isEmpty() && toolCallTries < MAX_TOOL_CALL_RETRIES; ++toolCallTries) {
                for (ToolCall toolCall : toolCalls) {
                    executeFunction(toolCall, messages, calledFunctions);

                    result = sendChatRequest(messages);
                    toolCalls = result.getToolCalls();
                }
            }
            return new FunctionResponse(result.getContent(), calledFunctions.toString());
        } catch (Exception e) {
            throw new LLMServiceException("Could not call functions for prompt: " + prompt, e);
        }
    }

    private ChatMessage.ResponseMessage sendChatRequest(List<ChatMessage> messages) throws IOException {
        ChatRequest chatRequest = ChatRequest.builder()
                .tools(functionExecutor.getToolFunctions())
                .messages(messages)
                .build();
        return sendPostRequest(
                API_ENDPOINT.CHAT_COMPLETION.getUrl(),
                chatRequest,
                Chat.class
        ).firstMessage();
    }

    private void executeFunction(ToolCall toolCall, List<ChatMessage> messages, StringBuilder calledFunctions) throws LLMServiceException {
        FunctionCall function = toolCall.getFunction();
        String functionName = function.getName();
        String arguments = function.getArguments();

        String result = functionExecutor.execute(function);

        String toolResult = functionName + "(" + arguments + ") : " + result;
        calledFunctions.append(toolResult).append("; ");
        LogUtil.info(toolResult);

        messages.add(ChatMessage.ToolMessage.of("[TOOL_RESULTS]" + toolResult + "[/TOOL_RESULTS]", functionName));
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
            TTSSpeakRequest speakRequest = new TTSSpeakRequest(message, voiceIds);
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
    public void startSpeechToText(int timeout) {
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
            String url = API_ENDPOINT.STT_START.getUrl();
            STTResponse speechToTextResponse = sendPostRequest(url, mapper.createObjectNode(), STTResponse.class);
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
            return sendGetRequest(url, HealthResponse.class, "player2-game-key", MOD_ID);
        } catch(Exception e) {
            throw new LLMServiceException("Failed to send health check", e);
        }
    }

    /**
     * Checks if service is reachable by pinging the {@link API_ENDPOINT#PING } endpoint.
     */
    @Override
    public void checkServiceIsReachable() throws LLMServiceException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + API_ENDPOINT.PING.getUrl()))
                    .HEAD()
                    .timeout(java.time.Duration.ofSeconds(3))
                    .build();
            sendRequest(request);
        } catch (Exception e) {
            throw new LLMServiceException("Player2 API is not reachable", e);
        }
    }

    @Override
    public double[] generateEmbedding(List<String> prompt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private <T> T sendPostRequest(String url, Object requestBody, Class<T> responseType) throws IOException {
        String requestJson = mapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();
        LogUtil.info(npcName + " - " + request.method() + " - " + request.uri() + ": " + requestJson);
        return sendRequest(request, responseType);
    }

    private <T> T sendGetRequest(String url, Class<T> responseType, String... headers) throws IOException {
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

    private <T> T sendRequest(HttpRequest request, Class<T> responseType) throws IOException {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status != 200) {
                throw new IOException(status + " - " + response.uri() + ": " + response.body());
            }
            LogUtil.info(npcName + " - " + response.statusCode() + " - " + response.uri() + ": " + mapper.writeValueAsString(response.body()));
            return mapper.readValue(response.body(), responseType);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    private void sendRequest(HttpRequest request) throws IOException {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status != 200) {
                throw new IOException(status + " - " + response.uri() + " responseBody: " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

}