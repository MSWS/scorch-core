package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

public class ACommand extends BukkitCommand {

	public ACommand(String name) {
		super(name);
		this.setPermission("scorch.command.a");
		this.setPermissionMessage(ScorchCore.getInstance().getMessages().getMessage("noperm").getMessage());
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length < 1) {
			MSG.tell(sender, "/a [message]");
			return true;
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++)
			builder.append(args[i] + " ");

		String msg = ScorchCore.getInstance().getMessages().getMessage("aformat").getMessage();
		msg = msg
				.replace("%prefix%",
						(sender instanceof Player) ? ScorchCore.getInstance().getPrefix((OfflinePlayer) sender) + ""
								: "&4")
				.replace("%player%", sender.getName()).replace("%message%", builder.toString().trim());

		for (Player p : Bukkit.getOnlinePlayers())
			if (p.hasPermission("scorch.command.a.receive") || p.equals(sender)) {
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);
				MSG.tell(p, msg);
			}
		return true;
	}

}
