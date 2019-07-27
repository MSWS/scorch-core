package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.PlaytimeModule;
import com.scorch.core.utils.MSG;

/**
 * Get playtime of players
 * 
 * <b>Permissions</b><br>
 * scorch.command.playtime - Access to command scorch.command.playtime.others -
 * View other player's playtimes
 * 
 * @author imodm
 *
 */
public class PlaytimeCommand extends BukkitCommand {
	public PlaytimeCommand(String name) {
		super(name);
		setPermission("scorch.command.playtime");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		OfflinePlayer target = null;
		if (args.length == 0) {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				MSG.tell(sender, "Define player");
				return true;
			}
		} else if (sender.hasPermission("scorch.command.playtime.others")) {
			target = Bukkit.getOfflinePlayer(args[0]);
		} else {
			MSG.tell(sender, ScorchCore.getInstance().getMessage("noperm"));
			return true;
		}

		long time = ScorchCore.getInstance().getModule("PlaytimeModule", PlaytimeModule.class)
				.getPlaytime(target.getUniqueId());

		if (time == 0) {
			MSG.tell(sender, ScorchCore.getInstance().getMessage("noplaytime").replace("%player%", target.getName()));
			return true;
		}

		MSG.tell(sender, ScorchCore.getInstance().getMessage("playtimeformat").replace("%player%", target.getName())
				.replace("%time%", MSG.getTime(time)));
		return true;
	}
}
