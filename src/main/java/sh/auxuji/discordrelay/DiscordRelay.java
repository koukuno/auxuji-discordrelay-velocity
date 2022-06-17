// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.discordrelay;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer;
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializer;
import java.nio.file.Path;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import sh.auxuji.discordrelay.commands.ReloadConfigCommand;
import org.slf4j.Logger;

@Plugin(id = "auxuji-discordrelay", name = "DiscordRelay", version = "1.0-SNAPSHOT",
	description = "Velocity <-> Discord Relay Plugin for Velocity", authors = {"koukuno"})
public class DiscordRelay {
	private final Logger logger;
	private final ProxyServer server;
	private final Path dataDir;

	private PluginConfig config;
	private DiscordInstance discordInstance;

	@Inject
	public DiscordRelay(ProxyServer server, Logger logger, @DataDirectory Path dataDir) {
		this.server = server;
		this.logger = logger;
		this.dataDir = dataDir;
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		this.config = new PluginConfig(this.logger, this.dataDir);
		this.discordInstance = new DiscordInstance(this);

		CommandManager commandManager = this.server.getCommandManager();
		CommandMeta meta = commandManager.metaBuilder("discordrelay-reloadconfig").build();
		commandManager.register(meta, new ReloadConfigCommand(this));

		this.server.getEventManager().register(this, new VelocityPlayerChatListener(this));
	}

	// broadcast to all connected players in the proxy
	public void broadcastMessage(String where, String user, String messageBody) {
		String message = String.format("[%s] %s: %s", where, user, messageBody);
		for (Player player : this.server.getAllPlayers())
			player.sendMessage(Identity.nil(), MinecraftSerializer.INSTANCE.serialize(message));
	}

	// broadcast to all webhooks
	public void discordBroadcastMessage(String where, String user, Component message) {
		this.discordInstance.broadcastMessage(String.format("[%s] %s", where, user), DiscordSerializer.INSTANCE.serialize(message));
	}

	public void reloadConfig() {
		this.logger.info("Reload config");
		this.config.loadConfig();
		this.discordInstance.close();
		this.discordInstance = new DiscordInstance(this);
	}

	public Logger getLogger() {
		return this.logger;
	}

	public ProxyServer getServer() {
		return this.server;
	}

	public PluginConfig getConfig() {
		return this.config;
	}
}
