package com.scorch.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

public class TestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			MSG.tell(sender, "/test [argument]");
			return true;
		}

		MSG.tell(sender, ScorchCore.getInstance().getMessages().getMessage(args[0]));
		return true;
	}

}
