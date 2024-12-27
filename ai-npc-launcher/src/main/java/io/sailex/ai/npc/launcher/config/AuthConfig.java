package io.sailex.ai.npc.launcher.config;

import io.sailex.ai.npc.launcher.constants.ConfigConstants;
import io.sailex.ai.npc.launcher.util.LogUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AuthConfig extends AConfig {

	public AuthConfig() {
		super("auth-config");
	}

	@Override
	protected void setDefaultProperties() {
		properties.setProperty(ConfigConstants.AUTH_CREDENTIALS, "");
	}

	public Map<String, String> getPropertyMap(String key) {
		try {
			String property = getProperty(key);
			Map<String, String> credentialsMap = new HashMap<>();
			if (property.isEmpty()) {
				return credentialsMap;
			}
			Arrays.stream(property.split(";"))
					.map(credential -> credential.split("="))
					.forEach(pair -> credentialsMap.put(pair[0], pair[1]));
			return credentialsMap;
		} catch (Exception e) {
			LogUtil.error("Failed to get property map for key: " + key);
			return new HashMap<>();
		}
	}
}
