// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.discordrelay;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import org.slf4j.Logger;

public class PluginConfig {
	// Increment if config file structure changes
	private static final long SUPPORTED_CONFIG_VERSION = 1;

	private final Logger logger;
	private final Path dataDir;

	private String discordApiToken;
	private List<Long> discordChannelIds;
	private long discordTimeout;

	@Inject
	public PluginConfig(Logger logger, @DataDirectory Path dataDir) {
		this.logger = logger;
		this.dataDir = dataDir;
		this.loadConfig();
	}

	// Load config and do basic config checks (the checks should not be comprehensive)
	public void loadConfig() {
		File dataDirFile = this.dataDir.toFile();
		if (!dataDirFile.exists())
			dataDirFile.mkdir();

		File configFile = new File(dataDirFile, "config.toml");
		if (!configFile.exists()) {
			try {
				// copy the default config from resources into plugin data directory
				InputStream defaultConfigStream = getClass().getResourceAsStream("/config.toml");
				Files.copy(defaultConfigStream, configFile.toPath());
			} catch (Exception e) {
				throw new RuntimeException("Cannot copy default config file!");
			}
		}

		Toml toml = new Toml().read(configFile);

		long configVersion = toml.getLong("config.version");
		if (configVersion != SUPPORTED_CONFIG_VERSION) {
			this.logger.warn(String.format("Unsupported config version: %ld (plugin only supports: %ld)", configVersion, SUPPORTED_CONFIG_VERSION));
			return;
		}

		String apiToken = toml.getString("discord.apiToken");
		if (apiToken == null || apiToken.equals("null")) {
			this.logger.warn("No Discord Bot API token specified! Please configure one.");
		} else {
			this.discordApiToken = apiToken;
		}

		List<Long> channelIds = toml.getList("discord.channelIds");
		if (channelIds == null || channelIds.size() == 0) {
			this.logger.warn("No Discord text channel IDs specified! Please configure at least one.");
		} else {
			this.discordChannelIds = channelIds;
		}

		// Unused
		this.discordTimeout = toml.getLong("discord.timeout");
		if (this.discordTimeout <= 0)
			this.logger.warn("Timeout of 0 or below will lead to missing messages!");
	}

	public String getDiscordApiToken() {
		return this.discordApiToken;
	}

	public List<Long> getDiscordChannelIds() {
		return this.discordChannelIds;
	}

	public long getDiscordTimeout() {
		return this.discordTimeout;
	}
}
