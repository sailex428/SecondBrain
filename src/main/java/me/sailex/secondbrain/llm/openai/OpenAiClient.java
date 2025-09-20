package me.sailex.secondbrain.llm.openai;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import me.sailex.secondbrain.exception.LLMServiceException;
import me.sailex.secondbrain.history.ConversationHistory;
import me.sailex.secondbrain.llm.ALLMClient;
import me.sailex.secondbrain.llm.function_calling.FunctionProvider;
import me.sailex.secondbrain.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//currently not used
//public class OpenAiClient extends ALLMClient<FunctionDef> {
//
//	private final SimpleOpenAI openAiService;
//	private final String openAiModel;
//	private final int timeout;
//
//	private final FunctionExecutor functionExecutor;
//
//	/**
//	 * Constructor for OpenAiClient.
//	 *
//	 * @param apiKey  the api key
//	 */
//	public OpenAiClient(FunctionProvider<FunctionDef> functionManager, String apiKey, int timeout) {
//        super(functionManager);
//		this.openAiModel = "gpt-4o-mini";
//		this.openAiService =
//				SimpleOpenAI.builder().apiKey(apiKey).build();
//		this.timeout = timeout;
//		this.functionExecutor = new FunctionExecutor();
//	}
//
//	/**
//	 * Executes functions that are called by openai based on the prompt and registered functions.
//	 */
//	@Override
//	public void callFunctions(ConversationHistory history) {
//		try {
//			StringBuilder calledFunctions = new StringBuilder();
//            ChatMessage.ResponseMessage responseMessage = new ChatMessage.ResponseMessage();
//			responseMessage.setContent("empty response");
//
//           	for (int i = 0; i < functions.size(); i++) {
//				functionExecutor.enrollFunctions(functions);
//                ChatRequest chatRequest = ChatRequest.builder()
//                        .model(openAiModel)
//						.tools(functionExecutor.getToolFunctions())
//                        .message(ChatMessage.ResponseMessage.)
//                        .build();
//
//                responseMessage = openAiService
//						.chatCompletions()
//						.create(chatRequest)
//						.get(timeout, TimeUnit.SECONDS)
//						.firstMessage();
//
//				List<ToolCall> toolCalls = responseMessage.getToolCalls();
//				if (toolCalls == null || toolCalls.isEmpty()) {
//					break;
//				}
//				ToolCall toolCall = toolCalls.getFirst();
//				calledFunctions.append(toolCall.getFunction().getName())
//						.append(" - args: ")
//						.append(toolCall.getFunction().getArguments())
//						.append(StringUtils.SPACE);
//				executeFunctionCalls(toolCall);
//				removeCalledFunctions(functions, toolCall.getFunction().getName());
//            }
//        } catch (Exception e) {
//			Thread.currentThread().interrupt();
//			LogUtil.error("LLM has not called any functions for prompt: " , e);
//			throw new LLMServiceException();
//		}
//	}
//
//	private void removeCalledFunctions(List<FunctionDef> functions, String functionName) {
//		List<FunctionDef> functionsToRemove = new ArrayList<>();
//		functions.forEach(func -> {
//			if (func.getName().equals(functionName)) {
//				functionsToRemove.add(func);
//			}
//		});
//		functions.removeAll(functionsToRemove);
//	}
//
//	private void executeFunctionCalls(ToolCall toolCall) {
//		FunctionCall function = toolCall.getFunction();
//		LogUtil.info(("Executed function: %s - %s").formatted(function.getName(), function.getArguments()));
//		functionExecutor.execute(function);
//	}
//
//}
