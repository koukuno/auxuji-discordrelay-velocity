// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.discordrelay;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import java.util.List;

public class DiscordInstance {
	private DiscordRelay plugin;
	private JDA jda;
	private boolean init;
	private long userId;

	private DiscordWebhookCache webhookCache;

	public DiscordInstance(DiscordRelay plugin) {
		this.plugin = plugin;
		this.init = false;

		if (this.plugin.getConfig().getDiscordApiToken() == null)
			return;

		try {
			JDABuilder builder = JDABuilder.createDefault(this.plugin.getConfig().getDiscordApiToken());
			builder.setAutoReconnect(true);

			this.jda = builder.build();
			this.jda.awaitReady();
			this.jda.addEventListener(new DiscordTextChannelListener(this.plugin, this));
			this.userId = this.jda.getSelfUser().getIdLong();
			this.init = true;

			this.webhookCache = new DiscordWebhookCache(this);
			for (long channelId : this.plugin.getConfig().getDiscordChannelIds())
				this.webhookCache.setupCache(channelId);
		} catch (Exception e) {
			this.plugin.getLogger().warn("Failed to initialize Discord API!");
		}
	}

	public void broadcastMessage(String user, String messageBody) {
		WebhookMessageBuilder builder = new WebhookMessageBuilder();
		builder.setUsername(user).setContent(messageBody);

		try {
			this.webhookCache.getCluster().broadcast(builder.build());
		} catch (Exception e) {
			this.plugin.getLogger().warn("Failed to broadcast message to available webhooks!");
		}
	}

	public void close() {
		this.webhookCache.close();
		this.jda.shutdown();
	}

	public DiscordRelay getPlugin() {
		return this.plugin;
	}

	public JDA getJDA() {
		return this.jda;
	}

	public long getUserId() {
		return this.userId;
	}

	public boolean isInit() {
		return this.init;
	}

	public List<Long> getWebhookIds() {
		return this.webhookCache.getWebhookIds();
	}
}
