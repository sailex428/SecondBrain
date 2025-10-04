package me.sailex.secondbrain.llm.openai;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import me.sailex.secondbrain.exception.LLMServiceException;
import me.sailex.secondbrain.history.Message;
import me.sailex.secondbrain.history.MessageConverter;
import me.sailex.secondbrain.llm.LLMClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class OpenAiClient implements LLMClient {

	private final SimpleOpenAI openAiService;
	private final String openAiModel;
	private final int timeout;

	/**
	 * Constructor for OpenAiClient.
	 *
	 * @param apiKey  the api key
	 */
	public OpenAiClient(String model, String apiKey, int timeout) {
		this.openAiModel = model;
		this.openAiService = SimpleOpenAI.builder().apiKey(apiKey).build();
		this.timeout = timeout;
	}

	@Override
	public Message chat(List<Message> messages) {
		try {
            ChatRequest chatRequest = ChatRequest.builder()
                    .model(openAiModel)
                    .messages(messages.stream()
                            .map(MessageConverter::toChatMessage)
                            .toList())
                    .build();
            ChatMessage.ResponseMessage responseMessage = openAiService
                    .chatCompletions()
                    .create(chatRequest)
                    .get(timeout, TimeUnit.SECONDS)
                    .firstMessage();
            return MessageConverter.toMessage(responseMessage);
        } catch (Exception e) {
            throw new LLMServiceException("Could not generate Response for prompt: " + messages.getLast(), e);
        }
    }

    @Override
    public void checkServiceIsReachable() {
        //i guess its always reachable?
    }

    @Override
    public void stopService() {
        //theres also nothing to stop
	}

}
