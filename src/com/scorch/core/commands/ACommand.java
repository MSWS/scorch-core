package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

public class ACommand implements CommandExecutor {

	public ACommand() {
		PluginCommand cmd = ScorchCore.getInstance().getCommand("a");
		cmd.setExecutor(this);
		cmd.setPermission("scorch.command.a");
		cmd.setPermissionMessage(ScorchCore.getInstance().getMessages().getMessage("noperm").getMessage());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) {
			MSG.tell(sender, "/a [message]");
			return true;
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++)
			builder.append(args[i] + " ");

		MSG.tell("scorch.command.a.receive", "&6" + sender.getName() + "&d " + builder.toString().trim());
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.hasPermission("scorch.command.a.receive"))
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);
		return true;
	}

}
