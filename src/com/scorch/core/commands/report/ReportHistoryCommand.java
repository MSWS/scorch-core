package com.scorch.core.commands.report;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.report.ReportModule;
import com.scorch.core.utils.MSG;

public class ReportHistoryCommand extends BukkitCommand {

	public ReportHistoryCommand(String name) {
		super(name);
		setPermission("scorch.command.reporthistory");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player.");
			return true;
		}

		OfflinePlayer target = null;
		if (args.length == 0) {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				MSG.tell(sender, "You must be a player.");
				return true;
			}
		} else if (sender.hasPermission("scorch.command.reporthistory.others")) {
			target = Bukkit.getOfflinePlayer(args[0]);
		} else {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		Player player = (Player) sender;
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		player.openInventory(
				ScorchCore.getInstance().getModule("ReportModule", ReportModule.class).getReportsGUI(target, 0));
		sp.setTempData("openInventory", "reporthistory");
		sp.setTempData("viewing", target.getUniqueId());
		return true;
	}

}
