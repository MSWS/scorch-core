package com.scorch.core.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import com.scorch.core.utils.MSG;

/**
 * Custom help command that responds with database message
 * 
 * @author imodm
 *
 */
public class HelpCommand extends BukkitCommand {

	public HelpCommand(String name) {
		super(name);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		MSG.cTell(sender, "helpmessage");
		return true;
	}

}
