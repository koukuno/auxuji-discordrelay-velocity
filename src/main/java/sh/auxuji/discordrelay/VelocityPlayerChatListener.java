// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.discordrelay;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class VelocityPlayerChatListener {
	private final DiscordRelay plugin;

	public VelocityPlayerChatListener(DiscordRelay plugin) {
		this.plugin = plugin;
	}

	@Subscribe(order = PostOrder.EARLY)
	public void onPlayerChat(PlayerChatEvent event) {
		Optional<ServerConnection> serverConnection = event.getPlayer().getCurrentServer();
		String serverName = "UNKNOWN";
		if (serverConnection.isPresent())
			serverName = serverConnection.get().getServerInfo().getName();

		String playerUsername = event.getPlayer().getUsername();
		String message = event.getMessage();

		MiniMessage mm = MiniMessage.miniMessage();
		this.plugin.discordBroadcastMessage(serverName, playerUsername, mm.deserialize(message));
	}
}
