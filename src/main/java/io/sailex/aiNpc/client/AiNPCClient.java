package io.sailex.aiNpc.client;

import io.sailex.aiNpc.client.command.SetConfigCommand;
import io.sailex.aiNpc.client.config.ModConfig;
import io.sailex.aiNpc.client.constant.ConfigConstants;
import io.sailex.aiNpc.client.listener.EventListenerManager;
import io.sailex.aiNpc.client.llm.ILLMClient;
import io.sailex.aiNpc.client.llm.OllamaClient;
import io.sailex.aiNpc.client.llm.OpenAiClient;
import io.sailex.aiNpc.client.model.NPC;
import io.sailex.aiNpc.client.npc.NPCContextGenerator;
import io.sailex.aiNpc.client.npc.NPCController;
import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class AiNPCClient implements ClientModInitializer {

	private static final Logger LOGGER = LogManager.getLogger(AiNPCClient.class);
	public static final String MOD_ID = "ai_npc";
	public boolean npcInitialized = false;
	public static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {
		waitForClientToLoad();
	}

	private void waitForClientToLoad() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (client.currentScreen instanceof TitleScreen) {
				LOGGER.info("Connect to server");
				connectToServer();
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
		ConnectScreen.connect(
				client.currentScreen,
				client,
				new ServerAddress("127.0.0.1", 25565),
				new ServerInfo("local", "127.0.0.1", ServerInfo.ServerType.OTHER),
				true);
	}

	private void initializeNPC() {
		ClientPlayerEntity npcEntity = client.player;
		if (npcEntity == null) {
			LOGGER.error("NPC entity is null");
			client.stop();
			return;
		}

		ModConfig.init();
		registerCommands();

		ILLMClient llmService;
		if ("openai".equals(ModConfig.getProperty(ConfigConstants.NPC_LLM_TYPE))) {
			llmService = new OpenAiClient(ModConfig.getProperty(ConfigConstants.NPC_LLM_OPENAI_MODEL));
		} else {
			llmService = new OllamaClient(ModConfig.getProperty(ConfigConstants.NPC_LLM_OLLAMA_MODEL));
		}
		NPCContextGenerator npcContextGenerator = new NPCContextGenerator(npcEntity);
		NPCController controller = new NPCController(npcEntity, llmService, npcContextGenerator);
		NPC npc = new NPC(npcEntity.getUuid(), npcEntity, controller, npcContextGenerator, llmService);

		EventListenerManager eventListenerManager = new EventListenerManager(npc);
		eventListenerManager.register();
	}

	private void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> new SetConfigCommand().register(dispatcher));
	}
}
