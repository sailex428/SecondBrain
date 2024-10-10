package io.sailex.aiNpc.util;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import io.sailex.aiNpc.config.ConfigReader;
import io.sailex.aiNpc.constant.ConfigConstants;
import io.sailex.aiNpc.exception.ProfileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UserCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds and manages GameProfiles for NPCs in a Minecraft server environment.
 * This class handles the creation and retrieval of profiles through both online (Mojang authentication)
 * and offline modes.
 *
 * @see GameProfile
 * @author sailex
 */
public class GameProfileBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameProfileBuilder.class);

	private final ConfigReader configReader;

	public GameProfileBuilder(ConfigReader configReader) {
		this.configReader = configReader;
	}

	/**
	 * Retrieves a GameProfile for an NPC based on the given name.
	 * If the server is in online mode, attempts to fetch the profile from Mojang's authentication service.
	 * Falls back to creating a new profile with a random UUID if no existing profile is found.
	 *
	 * @param npcName The name of the NPC to get the profile for
	 * @return GameProfile for the NPC, never null
	 */
	public GameProfile getGameProfile(String npcName, MinecraftServer server) {
		if (npcName == null || npcName.isBlank()) {
			throw new IllegalArgumentException("NPC name cannot be null or empty");
		}

		boolean useMojangAuth = server.isDedicated() && server.isOnlineMode();
		UserCache.setUseRemote(useMojangAuth);

		if (!useMojangAuth) {
			return createGameProfile(npcName);
		}

		// Try to find profile in user cache
		if (server.getUserCache() != null) {
			Optional<GameProfile> cachedProfile = server.getUserCache().findByName(npcName);
			if (cachedProfile.isPresent()) {
				return cachedProfile.get();
			}
		}

		return fetchGameProfile(npcName).orElseGet(() -> createGameProfile(npcName));
	}

	/**
	 * Creates a new GameProfile with a random UUID for the given name.
	 *
	 * @param name The name for the new profile
	 * @return A new GameProfile instance
	 */
	private GameProfile createGameProfile(String name) {
		return new GameProfile(UUID.randomUUID(), name);
	}

	/**
	 * Attempts to fetch a GameProfile from Mojang's authentication service.
	 *
	 * @param name The name of the profile to fetch
	 * @return Optional containing the fetched profile, or empty if not found
	 * @throws ProfileNotFoundException if there's an error while fetching the profile
	 */
	private Optional<GameProfile> fetchGameProfile(String name) throws ProfileNotFoundException {
		try {
			URL url = URI.create(configReader.getProperty(ConfigConstants.NPC_MOJANG_API_URL) + name)
					.toURL();
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");

			if (urlConnection.getResponseCode() == 200) {
				InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
				GameProfile profile = new Gson().fromJson(reader, GameProfile.class);
				return Optional.of(profile);
			}
		} catch (Exception e) {
			LOGGER.error("Profile {} was not found on api.mojang.com {}", name, e.getMessage());
			throw new ProfileNotFoundException(
					"Profile" + name + " was not found on api.mojang.com : " + e.getMessage());
		}
		return Optional.empty();
	}
}
