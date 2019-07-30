package com.scorch.core.commands.report;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.report.Report;
import com.scorch.core.modules.report.ReportModule;
import com.scorch.core.utils.MSG;

public class ReportCloseCommand extends BukkitCommand {

	public ReportCloseCommand(String name) {
		super(name);
		setPermission("scorch.command.reportclose");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("rc", "closereport", "rclose"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		ReportModule rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);

		if (!(sender.hasPermission(getPermission()))) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		if (args.length == 0) {
			MSG.tell(sender, "/" + commandLabel + " [resolution]");
			return true;
		}

		String resolution = String.join(" ", args);

		Player player = (Player) sender;
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		Report report = rm.getReport(sp.getData("assignedreport", String.class));
		if (report == null) {
			MSG.tell(sender, "You are not assigned to a report.");
			return true;
		}
		player.openInventory(rm.getResolutionGUI());
		sp.setTempData("closereason", resolution);
		sp.setTempData("openInventory", "reportclose");
		return true;
	}

}
