package io.sailex.ai.npc.launcher.commands;

import static net.minecraft.server.command.CommandManager.argument;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.ai.npc.launcher.util.LogUtil;
import me.earth.headlessmc.launcher.Launcher;
import me.earth.headlessmc.launcher.auth.AuthException;
import me.earth.headlessmc.launcher.auth.ValidatedAccount;
import net.lenni0451.commons.httpclient.HttpClient;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepCredentialsMsaCode;

/**
 * Command to log in into a Minecraft account that then can be used to spawn a NPC.
 *
 * @author sailex
 */
public class NPCLoginCommand {

	private final Launcher launcher;

	public NPCLoginCommand(Launcher launcher) {
		this.launcher = launcher;
	}

	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("login")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("email", StringArgumentType.string())
						.then(argument("password", StringArgumentType.string()).executes(this::executeLogin))));
	}

	private int executeLogin(CommandContext<ServerCommandSource> context) {
		String email = StringArgumentType.getString(context, "email");
		String password = StringArgumentType.getString(context, "password");

		ServerPlayerEntity source = context.getSource().getPlayer();
		if (source == null) {
			LogUtil.error("Please run this command as a player!", true);
			return 1;
		}
		login(email, password, source);
		return 0;
	}

	private void login(String email, String password, ServerPlayerEntity source) {
		try {
			HttpClient httpClient = MinecraftAuth.createHttpClient();
			StepFullJavaSession.FullJavaSession javaSession = MinecraftAuth.JAVA_CREDENTIALS_LOGIN.getFromInput(
					httpClient, new StepCredentialsMsaCode.MsaCredentials(email, password));
			saveAccount(javaSession, source);
		} catch (Exception e) {
			source.sendMessage(LogUtil.formatError("Failed to login with email: " + email));
		}
	}

	private void saveAccount(StepFullJavaSession.FullJavaSession fullJavaSession, ServerPlayerEntity source) {
		ValidatedAccount validatedAccount;
		try {
			validatedAccount =
					launcher.getAccountManager().getAccountValidator().validate(fullJavaSession);
		} catch (AuthException e) {
			source.sendMessage(LogUtil.formatError("Failed to validate account!"));
			return;
		}
		source.sendMessage(LogUtil.formatInfo("Logged into account " + validatedAccount.getName()
				+ " successfully! Now you can spawn a NPC with this account."));
		launcher.getAccountManager().addAccount(validatedAccount);
	}
}
