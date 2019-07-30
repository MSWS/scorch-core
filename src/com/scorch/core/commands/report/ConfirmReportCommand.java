package com.scorch.core.commands.report;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

public class ConfirmReportCommand extends BukkitCommand {

	public ConfirmReportCommand(String name) {
		super(name);
		setPermission("scorch.command.reportclose");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!(sender.hasPermission(getPermission()))) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length < 2) {
			MSG.tell(sender, "/" + commandLabel + " [ID] [Reason]");
			return true;
		}

		String reason = "";
		for (int i = 1; i < args.length; i++) {
			reason += args[i] + " ";
		}
		reason = reason.trim();

		String result = MSG.hashWithSalt(args[0], reason, 16, 5);
		MSG.tell(sender, "&7The report hash should be &e" + result);
		return true;
	}

}
