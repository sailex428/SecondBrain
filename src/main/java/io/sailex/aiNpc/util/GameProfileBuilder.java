package io.sailex.aiNpc.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UserCache;

/**
 * Builds and manages GameProfiles for NPCs in a Minecraft server environment.
 * This class handles the creation and retrieval of profiles through both online (Mojang api)
 * and offline modes.
 *
 * @see GameProfile
 * @author sailex
 */
public class GameProfileBuilder {

	/**
	 * Retrieves a GameProfile for an NPC based on the given name.
	 * If the server is in online mode, attempts to fetch the profile from Mojang's api service.
	 * Falls back to creating a new profile with a random UUID if no existing profile is found.
	 *
	 * @param npcName The name of the NPC to get the profile for
	 * @return GameProfile for the NPC, never null
	 */
	public GameProfile getGameProfile(String npcName, MinecraftServer server) {
		if (npcName == null || npcName.isBlank()) {
			throw new IllegalArgumentException("NPC name cannot be null or empty");
		}

		boolean useMojangApi = server.isDedicated() && server.isOnlineMode();
		UserCache.setUseRemote(useMojangApi);

		if (!useMojangApi) {
			return createGameProfile(npcName);
		}

		// Try to find profile using mojang api
		if (server.getUserCache() != null) {
			Optional<GameProfile> cachedProfile = server.getUserCache().findByName(npcName);
			if (cachedProfile.isPresent()) {
				return fetchProfileWithSkin(server, cachedProfile.get());
			}
		}
		return createGameProfile(npcName);
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
	 * Fetches a GameProfile with skin data from Mojang's api service.
	 *
	 * @param server The Minecraft server instance
	 * @param cachedProfile The cached profile to fetch the skin for
	 * @return GameProfile with skin data
	 */
	private GameProfile fetchProfileWithSkin(MinecraftServer server, GameProfile cachedProfile) {
		ProfileResult profileWithSkin = server.getSessionService().fetchProfile(cachedProfile.getId(), true);
		if (profileWithSkin != null) {
			return profileWithSkin.profile();
		}
		return cachedProfile;
	}
}
