package io.sailex.ai.npc.launcher.constants;

import me.earth.headlessmc.launcher.specifics.VersionSpecificModRepository;
import me.earth.headlessmc.launcher.util.URLs;

public class ModRepositories {

	private ModRepositories() {}

	public static final VersionSpecificModRepository AI_NPC = new VersionSpecificModRepository(
			URLs.url("https://github.com/sailex428/AI-NPC/releases/download/"), "ai-npc", "v1.0.8", "-beta");
}
