package io.sailex.ai.npc.client;

import io.sailex.ai.npc.client.config.Config;
import io.sailex.ai.npc.client.constant.ConfigConstants;
import io.sailex.ai.npc.client.listener.EventListenerRegisterer;
import io.sailex.ai.npc.client.llm.ILLMClient;
import io.sailex.ai.npc.client.llm.OllamaClient;
import io.sailex.ai.npc.client.llm.OpenAiClient;
import io.sailex.ai.npc.client.model.NPC;
import io.sailex.ai.npc.client.npc.NPCContextGenerator;
import io.sailex.ai.npc.client.npc.NPCController;
import io.sailex.ai.npc.client.util.ConnectionUtil;
import io.sailex.ai.npc.client.database.index.DefaultResourcesIndexer;
import io.sailex.ai.npc.client.database.repositories.RepositoryFactory;

import lombok.Getter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Main class for the AI NPC client mod.
 * Initializes the NPC, its components, and connects to the server.
 */
@Getter
public class AiNPCClient implements ClientModInitializer {

	private static final Logger LOGGER = LogManager.getLogger(AiNPCClient.class);
	public static final String MOD_ID = "ai_npc";
	public static final MinecraftClient client = MinecraftClient.getInstance();

	private boolean npcInitialized = false;
	private boolean connected = false;

	/**
	 * Initialize the client mod.
	 * Waits for the client to load and then init the NPC and connects to the server.
	 */
	@Override
	public void onInitializeClient() {
		waitForClientToLoad();
	}

	private void waitForClientToLoad() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.isFinishedLoading() && !connected) {
				client.getSoundManager().close();
				ConnectionUtil.connectToServer();
				connected = true;
			}
		});
		ClientTickEvents.END_WORLD_TICK.register(world -> {
			if (!npcInitialized) {
				LOGGER.info("Initialize NPC");
				initializeNpc();
				npcInitialized = true;
				LOGGER.info("NPC successfully initialized");
			}
		});
	}

	private void initializeNpc() {
		ClientPlayerEntity npcEntity = client.player;
		if (npcEntity == null) {
			LOGGER.error("NPC entity is null");
			return;
		}
		ILLMClient llmClient = initLLMClient();
		RepositoryFactory repositoryFactory = new RepositoryFactory(llmClient);
		repositoryFactory.initRepositories();
		//? if <1.21.2 {
		indexDefaultResources(llmClient, repositoryFactory);
		//?}

		NPCContextGenerator npcContextGenerator = new NPCContextGenerator(npcEntity);
		NPCController controller = new NPCController(npcEntity, llmClient, npcContextGenerator, repositoryFactory);
		NPC npc = new NPC(npcEntity.getUuid(), npcEntity, controller, npcContextGenerator, llmClient);

		EventListenerRegisterer eventListenerRegisterer = new EventListenerRegisterer(npc);
		eventListenerRegisterer.registerListeners(repositoryFactory.getSqliteClient());
	}

	private ILLMClient initLLMClient() {
		ILLMClient llmService;
		String npcType = Config.getProperty(ConfigConstants.NPC_LLM_TYPE);
		if ("ollama".equals(npcType)) {
			String ollamaModel = Config.getProperty(ConfigConstants.NPC_LLM_OLLAMA_MODEL);
			String ollamaUrl = Config.getProperty(ConfigConstants.NPC_LLM_OLLAMA_URL);
			llmService = new OllamaClient(ollamaModel, ollamaUrl);
			llmService.checkServiceIsReachable();
		} else if ("openai".equals(npcType)) {
			String apiKey = Config.getProperty(ConfigConstants.NPC_LLM_OPENAI_API_KEY);
			String openAiModel = Config.getProperty(ConfigConstants.NPC_LLM_OPENAI_MODEL);
			llmService = new OpenAiClient(openAiModel, apiKey);
		} else {
			throw new IllegalArgumentException("Invalid LLM type: " + npcType);
		}
		return llmService;
	}

	//? if <1.21.2 {
	private void indexDefaultResources(ILLMClient llmClient, RepositoryFactory repositoryFactory) {
		DefaultResourcesIndexer defaultResourcesIndexer = new DefaultResourcesIndexer(
				repositoryFactory.getRequirementsRepository(),
				repositoryFactory.getTemplatesRepository(), llmClient);
		CompletableFuture.runAsync(() -> {
			defaultResourcesIndexer.indexRequirements();
			defaultResourcesIndexer.indexTemplates();
		});
	}
	//?}

}
