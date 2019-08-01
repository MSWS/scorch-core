package com.scorch.core.commands.staff;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

/**
 * Punish command meant for staff
 * 
 * <b>Permissions</b><br>
 * scorch.command.punish - Access to command<br>
 * (Modify GUI to define permissions for each item/punishment)
 * 
 * @author imodm
 *
 */
public class PunishCommand extends BukkitCommand {

	public PunishCommand(String name) {
		super(name);
		this.setPermission("scorch.command.punish");
		this.setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		this.setAliases(Arrays.asList("p"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		Player player = (Player) sender;

		if (args.length < 2) {
			MSG.tell(sender, "/punish [player] [reason]");
			return true;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < args.length; i++)
			sb.append(args[i] + " ");

		String reason = sb.toString().trim();

		OfflinePlayer target;

		if (args[0].length() > 16) {
			try {
				target = Bukkit.getOfflinePlayer(UUID.fromString(args[0]));
			} catch (IllegalArgumentException expected) {
				target = Bukkit.getOfflinePlayer(args[0]);
			}
		} else {
			target = Bukkit.getOfflinePlayer(args[0]);
		}

		ScorchCore.getInstance().getPunishModule().openPunishGUI(player, target, reason);
		return true;
	}

}
