package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.punish.PunishType;
import com.scorch.core.modules.punish.Punishment;
import com.scorch.core.utils.MSG;

public class PunishCommand implements CommandExecutor, TabCompleter {

	public PunishCommand() {
		PluginCommand cmd = Bukkit.getPluginCommand("punish");
		cmd.setExecutor(this);
		cmd.setTabCompleter(this);
		cmd.setPermission("scorch.command.punish");
		cmd.setPermissionMessage(ScorchCore.getInstance().getMessages().getMessage("noperm").toString()); // TODO
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

		if (args.length < 3) {
			MSG.tell(sender, "/punish [player] [type] [reason] d:<duration>");
			return true;
		}

		String reason = "";

		long duration = -1;

		for (int i = 2; i < args.length; i++) {
			if (args[i].toLowerCase().startsWith("d:")) {
				duration = (long) MSG.getMills(args[i].toLowerCase().substring(2));
				continue;
			}

			reason += args[i] + " ";
		}

		reason = reason.trim();

		Punishment punishment = new Punishment(target.getUniqueId(), sender.getName(), reason,
				System.currentTimeMillis(), duration, PunishType.valueOf(args[1].toUpperCase()));

		ScorchCore.getInstance().getPunishModule().addPunishment(punishment);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> result = new ArrayList<String>();

		if (args.length <= 1) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(p.getName());
			}
		} else if (args.length <= 2) {
			for (PunishType type : PunishType.values()) {
				if (type.toString().toLowerCase().startsWith(args[1].toLowerCase())) {
					result.add(type.toString());
				}
			}
		}

		return result;
	}

}
