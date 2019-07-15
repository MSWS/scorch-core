package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.punish.Punishment;
import com.scorch.core.utils.MSG;

public class UnpunishCommand implements CommandExecutor {

	public UnpunishCommand() {
		PluginCommand cmd = Bukkit.getPluginCommand("unpunish");
		cmd.setExecutor(this);
		cmd.setPermissionMessage(ScorchCore.getInstance().getMessages().getMessage("noperm").toString()); // TODO
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			MSG.tell(sender, ScorchCore.getInstance().getMessages().getMessage("noperm").toString());
			return true;
		}

		if (args.length < 1) {
			MSG.tell(sender, "/unpunish [player]");
			return true;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

		for (Punishment p : ScorchCore.getInstance().getPunishModule().getPunishments(target.getUniqueId())) {
			p.remove("CONSOLE", "Remover");
		}

		MSG.tell(sender, "Successfully unbanned " + target.getName());
		return true;
	}
}
