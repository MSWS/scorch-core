package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.commands.CommandModule;
import com.scorch.core.utils.MSG;

public class ToggleCommand extends BukkitCommand {

	private List<Command> commands;

	public ToggleCommand(String name) {
		super(name);
		setPermission("scorch.command.toggle");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		CommandModule mod = ScorchCore.getInstance().getModule("CommandModule", CommandModule.class);
		if (commands == null) {
			commands = new ArrayList<>(mod.getCommands().keySet());
		}

		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length == 0) {
			MSG.tell(sender, "/toggle [command] <true/false>");
			return true;
		}

		Command cmd = mod.getCommand(args[0].toLowerCase());

		if (cmd == null) {
			MSG.tell(sender, "Unknown Command");
			return true;
		}

		boolean status = !mod.isEnabled(cmd);

		if (args.length == 2) {
			status = Boolean.parseBoolean(args[1]);
		}

		if (status) {
			mod.enableCommand(cmd);
		} else {
			mod.disableCommand(cmd);
		}

		String msg = ScorchCore.getInstance().getMessage("commandtoggleformat").replace("%command%", cmd.getName())
				.replace("%status%", status ? "&aenabled" : "&cdisabled");

		MSG.tell(sender, msg);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();
		CommandModule mod = ScorchCore.getInstance().getModule("CommandModule", CommandModule.class);
		if (commands == null) {
			commands = new ArrayList<>(mod.getCommands().keySet());
		}

		if (args.length == 1)
			for (Command cmd : commands)
				if (cmd.getName().toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(cmd.getName());

		if (args.length == 2)
			for (String bool : new String[] { "true", "false" })
				if (bool.toLowerCase().startsWith(args[1].toLowerCase()))
					result.add(bool);

		return result;
	}

}
