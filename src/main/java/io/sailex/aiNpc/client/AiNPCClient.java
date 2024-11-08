package io.sailex.aiNpc.client;

import io.sailex.aiNpc.client.constant.ConfigConstants;
import io.sailex.aiNpc.client.listener.EventListenerManager;
import io.sailex.aiNpc.client.llm.ILLMClient;
import io.sailex.aiNpc.client.llm.OllamaClient;
import io.sailex.aiNpc.client.llm.OpenAiClient;
import io.sailex.aiNpc.client.model.NPC;
import io.sailex.aiNpc.client.npc.NPCContextGenerator;
import io.sailex.aiNpc.client.npc.NPCController;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.*;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class AiNPCClient implements ClientModInitializer {

	private static final Logger LOGGER = LogManager.getLogger(AiNPCClient.class);
	public static final String MOD_ID = "ai_npc";
	public static final MinecraftClient client = MinecraftClient.getInstance();
	private boolean npcInitialized = false;
	private Properties properties;
	private boolean connected = false;

	@Setter
	private Text status = Text.of("connecting");

	@Override
	public void onInitializeClient() {
		properties = System.getProperties();
		waitForClientToLoad();
	}

	private void waitForClientToLoad() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.isFinishedLoading() && !connected) {
				connectToServer();
				connected = true;
			}
		});
		ClientTickEvents.END_WORLD_TICK.register(client -> {
			if (!npcInitialized) {
				LOGGER.info("Initialize NPC");
				initializeNPC();
				npcInitialized = true;
			}
		});
	}

	private void connectToServer() {
		String serverName = properties.getOrDefault("server.name", "localhost").toString();
		int port =
				Integer.parseInt(properties.getOrDefault("server.port", 25565).toString());

		ConnectScreen.connect(
				client.currentScreen,
				client,
				new ServerAddress(serverName, port),
				new ServerInfo("server", serverName, ServerInfo.ServerType.OTHER),
				false);
	}

	private void initializeNPC() {
		ClientPlayerEntity npcEntity = client.player;
		if (npcEntity == null) {
			LOGGER.error("NPC entity is null");
			client.stop();
			return;
		}

		ILLMClient llmService;
		String npcType = validateProperty(properties.getProperty(ConfigConstants.NPC_LLM_TYPE));
		if ("ollama".equals(npcType)) {
			String ollamaModel = validateProperty(properties.getProperty(ConfigConstants.NPC_LLM_OLLAMA_MODEL));
			String ollamaUrl = validateProperty(properties.getProperty(ConfigConstants.NPC_LLM_OLLAMA_URL));
			llmService = new OllamaClient(ollamaModel, ollamaUrl);
		} else {
			String apiKey = validateProperty(properties.getProperty(ConfigConstants.NPC_LLM_OPENAI_API_KEY));
			String openAiModel = validateProperty(properties.getProperty(ConfigConstants.NPC_LLM_OPENAI_MODEL));
			llmService = new OpenAiClient(openAiModel, apiKey);
		}

		NPCContextGenerator npcContextGenerator = new NPCContextGenerator(npcEntity);
		NPCController controller = new NPCController(npcEntity, llmService, npcContextGenerator);
		NPC npc = new NPC(npcEntity.getUuid(), npcEntity, controller, npcContextGenerator, llmService);

		EventListenerManager eventListenerManager = new EventListenerManager(npc);
		eventListenerManager.register();
	}

	private String validateProperty(String property) {
		LOGGER.info("Property: {}", property);
		if (property == null) {
			LOGGER.error("Property is null");
			client.stop();
			return null;
		}
		return property;
	}
}
