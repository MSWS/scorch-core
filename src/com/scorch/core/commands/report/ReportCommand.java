package com.scorch.core.commands.report;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.punish.PunishType;
import com.scorch.core.modules.punish.Punishment;
import com.scorch.core.modules.report.Report;
import com.scorch.core.modules.report.ReportModule;
import com.scorch.core.utils.MSG;

public class ReportCommand extends BukkitCommand {

	private ReportModule rm;

	public ReportCommand(String name) {
		super(name);
		setPermission("scorch.command.report");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (rm == null)
			rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);

		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		Player player = (Player) sender, target;
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		List<Punishment> history = ScorchCore.getInstance().getPunishModule().getPunishments(player.getUniqueId());
		Punishment p = history.stream().filter(h -> h.getType() == PunishType.REPORT_BAN && h.isActive()).findFirst()
				.orElse(null);

		if (p != null) {
			MSG.tell(sender, ScorchCore.getInstance().getMessage("reportbanmessage")
					.replace("%staff%", p.getStaffName()).replace("%reason%", p.getReason()));
			return true;
		}

		if (args.length < 2) {
			MSG.tell(sender, "/report [Player] [Reason]");
			return true;
		}

		target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			MSG.tell(sender, "Unknown Player.");
			return true;
		}

		List<Report> reports = rm.getReports(player.getUniqueId());
		if (reports.stream().filter(r -> r.isOpen()).collect(Collectors.toList()).size() >= getMaxReports(player)) {
			MSG.tell(sender, "&cYou've met the limit on reports you can make. (&e" + getMaxReports(player) + "&c)");
			return true;
		}

		String reason = "";
		for (int i = 1; i < args.length; i++) {
			reason += args[i] + " ";
		}
		reason = reason.trim();

		player.openInventory(rm.getReportGUI("Reporting " + target.getName() + "...", false));
		sp.setTempData("openInventory", "report");
		sp.setTempData("reporting", target.getUniqueId());
		sp.setTempData("reason", reason);
		return true;
	}

	private int getMaxReports(CommandSender sender) {
		for (int i = 100; i >= 0; i--) {
			if (sender.hasPermission("scorch.command.report." + i))
				return i;
		}
		return -1;
	}

}
