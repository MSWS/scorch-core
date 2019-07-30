package com.scorch.core.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.report.ReportModule;
import com.scorch.core.utils.MSG;

public class ReportHandleCommand extends BukkitCommand {

	public ReportHandleCommand(String name) {
		super(name);
		setPermission("scorch.command.reporthandle");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("rh", "handlereport", "handle"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		ReportModule rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);

		Player player = (Player) sender;
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		if (sp.hasData("assignedreport")) {
			MSG.tell(sender, "You already are assigned on report " + sp.getData("assignedreport", String.class));
			return true;
		}

		player.openInventory(rm.getReportGUI("Handling Report..."));
		sp.setTempData("openInventory", "reporthandle");
		return true;
	}

}
