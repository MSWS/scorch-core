package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

public class TestCommand implements CommandExecutor {

	public TestCommand() {
		PluginCommand cmd = Bukkit.getPluginCommand("test");
		cmd.setExecutor(this);
		cmd.setPermission("scorch.command.test");
		cmd.setPermissionMessage("NOPE");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			MSG.tell(sender, "/test [argument]");
			return true;
		}

		MSG.tell(sender, ScorchCore.getInstance().getMessages().getMessage(args[0])+" []");
		return true;
	}

}
