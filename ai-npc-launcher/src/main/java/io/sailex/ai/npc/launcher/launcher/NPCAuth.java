package io.sailex.ai.npc.launcher.launcher;

import io.sailex.ai.npc.launcher.AiNPCLauncher;
import io.sailex.ai.npc.launcher.config.AuthConfig;
import io.sailex.ai.npc.launcher.constants.ConfigConstants;
import io.sailex.ai.npc.launcher.util.LogUtil;
import java.awt.*;
import java.net.URI;
import java.util.Map;
import lombok.Setter;
import me.earth.headlessmc.launcher.auth.AuthException;
import me.earth.headlessmc.launcher.auth.LaunchAccount;
import me.earth.headlessmc.launcher.auth.ValidatedAccount;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepCredentialsMsaCode;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;

public class NPCAuth {

	@Setter
	private ClientLauncher clientLauncher;

	private final AuthConfig config;

	public NPCAuth(AuthConfig config) {
		this.config = config;
	}

	public boolean login() {
		if (AiNPCLauncher.getServer().isDedicated()) {
			return loginDedicated();
		}
		return loginDeviceCode();
	}

	private boolean loginDedicated() {
		Map<String, String> credentials = config.getPropertyMap(ConfigConstants.AUTH_CREDENTIALS);
		if (credentials.isEmpty()) {
			LogUtil.error("No credentials found");
			return false;
		}
		for (Map.Entry<String, String> entry : credentials.entrySet()) {
			if (!loginCredentials(entry.getKey(), entry.getValue())) {
				return false;
			}
		}
		return true;
	}

	private boolean loginCredentials(String email, String password) {
		try {
			LogUtil.info("Logging in with credentials");
			HttpClient httpClient = MinecraftAuth.createHttpClient();
			StepFullJavaSession.FullJavaSession javaSession = MinecraftAuth.JAVA_CREDENTIALS_LOGIN.getFromInput(
					httpClient, new StepCredentialsMsaCode.MsaCredentials(email, password));
			return saveAccount(javaSession);
		} catch (Exception e) {
			LogUtil.error("Failed to login with email: " + email);
			return false;
		}
	}

	private boolean loginDeviceCode() {
		try {
			LogUtil.info("Logging in with device code");
			HttpClient httpClient = MinecraftAuth.createHttpClient();
			StepFullJavaSession.FullJavaSession javaSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(
					httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(this::handleLoginPopup));
			return saveAccount(javaSession);
		} catch (Exception e) {
			LogUtil.error("Failed to login with device code!");
			return false;
		}
	}

	private void handleLoginPopup(StepMsaDeviceCode.MsaDeviceCode msaDeviceCode) {
		LogUtil.info("Go to", true);
		LogUtil.info(msaDeviceCode.getDirectVerificationUri(), true);
		try {
			URI url = URI.create(msaDeviceCode.getDirectVerificationUri());
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(url);
			} else {
				new ProcessBuilder("open", url.toString()).start();
			}
		} catch (Exception e) {
			LogUtil.error("Failed to open the verification URL automatically" + e);
		}
	}

	public LaunchAccount getSavedAccount(String npcName) {
		return clientLauncher.getLauncher().getAccountManager().getAccounts().stream()
				.map(ValidatedAccount::toLaunchAccount)
				.filter(account -> account.getName().equals(npcName))
				.findFirst()
				.orElse(null);
	}

	private boolean saveAccount(StepFullJavaSession.FullJavaSession fullJavaSession) {
		ValidatedAccount validatedAccount;
		try {
			validatedAccount = clientLauncher
					.getLauncher()
					.getAccountManager()
					.getAccountValidator()
					.validate(fullJavaSession);
		} catch (AuthException e) {
			LogUtil.error("Failed to validate account!");
			return false;
		}
		clientLauncher.getLauncher().getAccountManager().addAccount(validatedAccount);
		return true;
	}
}
