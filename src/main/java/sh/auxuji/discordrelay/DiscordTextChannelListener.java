// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.discordrelay;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;

public class DiscordTextChannelListener extends ListenerAdapter {
	private final DiscordRelay plugin;
	private final DiscordInstance instance;

	public DiscordTextChannelListener(DiscordRelay plugin, DiscordInstance instance) {
		this.plugin = plugin;
		this.instance = instance;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (plugin.getConfig().getDiscordChannelIds() == null)
			return;

		if (!event.isFromGuild() || event.getChannelType() != ChannelType.TEXT)
			return;

		User user = event.getAuthor();

		// if its from our webhooks, do not count it
		for (long id : this.instance.getWebhookIds()) {
			if (user.getIdLong() == id)
				return;
		}

		TextChannel textChannel = event.getTextChannel();
		for (long id : this.plugin.getConfig().getDiscordChannelIds()) {
			if (textChannel.getIdLong() == id) {
				String where = String.format("%s/#%s", event.getMessage().getGuild().getName(), textChannel.getName());
				String username = String.format("%s#%s", user.getName(), user.getDiscriminator());
				String messageBody = event.getMessage().getContentRaw();
				this.plugin.broadcastMessage(where, username, messageBody);
				break;
			}
		}
	}
}
