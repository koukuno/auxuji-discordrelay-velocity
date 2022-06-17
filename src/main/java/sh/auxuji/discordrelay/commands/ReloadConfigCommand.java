// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.discordrelay.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import sh.auxuji.discordrelay.DiscordRelay;

public class ReloadConfigCommand implements SimpleCommand {
	private final DiscordRelay plugin;

	public ReloadConfigCommand(DiscordRelay plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(Invocation invocation) {
		CommandSource source = invocation.source();
		if (source.hasPermission("auxuji-discordrelay.reloadconfig"))
			this.plugin.reloadConfig();
	}
};
