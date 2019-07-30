package com.scorch.core.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

/**
 * Get information on the latest build version that is currently on the server.
 * 
 * TODO: Link with online git repo
 * 
 * @author imodm
 *
 */
public class BVersionCommand extends BukkitCommand {

	public BVersionCommand(String name) {
		super(name);
		setPermission("scorch.command.bversion");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("bversion", "bv", "bver"));
	}

	// TODO

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}
		MSG.tell(sender, " ");
		MSG.tell(sender, "&9&lScorchCore &b[&8UN&b|&7A&b|&a0.0.2&b]");
		MSG.tell(sender, "&6&lLatest Push &e7/30/19");
		MSG.tell(sender, " ");

		return true;
	}

}
