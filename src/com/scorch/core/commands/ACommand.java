package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

/**
 * Staff Chat command, can be used by everyone but can only be seen by the
 * sender and staff in the same server
 * 
 * <b>Permissions</b><br>
 * scorch.command.a - Access to /a <br>
 * scorch.command.a.receive - View /a messages not sent by self
 * 
 * @author imodm
 *
 */
public class ACommand extends BukkitCommand {

	public ACommand(String name) {
		super(name);
		this.setPermission("scorch.command.a");
		this.setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
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

		String msg = ScorchCore.getInstance().getMessage("aformat");
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
