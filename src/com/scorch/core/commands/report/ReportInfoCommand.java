package com.scorch.core.commands.report;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.report.Report;
import com.scorch.core.modules.report.ReportModule;
import com.scorch.core.utils.MSG;

public class ReportInfoCommand extends BukkitCommand {

	public ReportInfoCommand(String name) {
		super(name);
		setPermission("scorch.command.reportinfo");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length == 0) {
			MSG.tell(sender, "/" + commandLabel + " [Report #]");
			return true;
		}

		ReportModule rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);

		Report report = rm.getReport(args[0]);
		if (report == null) {
			MSG.tell(sender, "&cInvalid Report #");
			return true;
		}

		rm.sendReportInfo(sender, report);
		return true;
	}

}
