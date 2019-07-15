package com.scorch.core.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.punish.Punishment;
import com.scorch.core.utils.MSG;

public class HistoryCommand implements CommandExecutor {

	public HistoryCommand() {
		PluginCommand cmd = Bukkit.getPluginCommand("history");
		cmd.setExecutor(this);
		cmd.setPermission("scorch.command.history");
		cmd.setPermissionMessage("NOPE");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			MSG.tell(sender, "/history [player]");
			return true;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

		List<Punishment> punishments = ScorchCore.getInstance().getPunishModule().getPunishments(target.getUniqueId());

		punishments.forEach(p -> MSG.tell(sender, p.getStaffName() + " " + p.getType() + " " + p.getReason()));

		return true;
	}

}
