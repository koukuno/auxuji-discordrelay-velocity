// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.discordrelay;

import club.minnced.discord.webhook.WebhookCluster;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordWebhookCache {
	private final DiscordInstance instance;
	private WebhookCluster cluster;
	private List<Long> webhookIds;

	public DiscordWebhookCache(DiscordInstance instance) {
		this.instance = instance;
		this.cluster = new WebhookCluster(this.instance.getPlugin().getConfig().getDiscordChannelIds().size());
		this.webhookIds = new ArrayList<Long>();
	}

	public void close() {
		this.cluster.close();
	}

	public void setupCache(long channelId) {
		try {
			TextChannel channel = this.instance.getJDA().getTextChannelById(channelId);

			this.instance.getPlugin().getLogger().info("Retrieve webhook from channel id " + channelId);

			List<Webhook> webhooks = channel.retrieveWebhooks().complete();
			for (Webhook webhook : webhooks) {
				if (webhook.getOwner().getIdLong() == this.instance.getUserId()) {
					this.cluster.buildWebhook(webhook.getIdLong(), webhook.getToken());
					this.webhookIds.add(webhook.getIdLong());
					return;
				}
			}

			// if the loop ends, create the webhook, since that means the webhook from the bot does not exist
			this.createWebhook(channelId);
		} catch (Exception e) {
			this.instance.getPlugin().getLogger().warn("Failed to retrieve webhook from channel id " + channelId);
		}
	}

	public void createWebhook(long channelId) {
		try {
			TextChannel channel = this.instance.getJDA().getTextChannelById(channelId);
			this.instance.getPlugin().getLogger().info("Create webhook for channel id " + channelId);
			Webhook webhook = channel.createWebhook("auxuji-discordrelay Webhook").reason("auxuji-discordrelay create webhook").complete();
			this.cluster.buildWebhook(webhook.getIdLong(), webhook.getToken());
			this.webhookIds.add(webhook.getIdLong());
		} catch (Exception e) {
			this.instance.getPlugin().getLogger().warn("Failed to create webhook for channel id " + channelId);
		}
	}

	public WebhookCluster getCluster() {
		return this.cluster;
	}

	public List<Long> getWebhookIds() {
		return this.webhookIds;
	}
}
