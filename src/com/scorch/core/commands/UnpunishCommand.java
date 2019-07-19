package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.punish.Punishment;
import com.scorch.core.utils.MSG;

public class UnpunishCommand extends BukkitCommand {

	public UnpunishCommand(String name) {
		super(name);
		this.setPermissionMessage(ScorchCore.getInstance().getMessages().getMessage("noperm").toString());
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
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
			p.remove("CONSOLE", "Removed by console command");
		}

		MSG.tell(sender, "Successfully removed all punishments of " + target.getName());
		return true;
	}
}
