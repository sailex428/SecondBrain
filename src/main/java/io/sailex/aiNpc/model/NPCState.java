package io.sailex.aiNpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

@Data
@AllArgsConstructor
public class NPCState {

	private double x;
	private double y;
	private double z;
	private RegistryKey<World> dimension;
	private float yaw;
	private float pitch;
	private float health;
	private String gameMode;
}
