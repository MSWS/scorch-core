package com.scorch.core.commands;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

/**
 * Tells the sender their ping
 * 
 * <b>Permissions</b><br>
 * scorch.command.ping - Access to command
 * 
 * @author imodm
 *
 */
public class PingCommand extends BukkitCommand {
	public PingCommand(String name) {
		super(name);
		setPermission("scorch.command.ping");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}
		
		if (!(sender instanceof Player)) {
			MSG.tell(sender, "must be player");
			return true;
		}

		Player target = null;

		if (args.length == 0) {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				MSG.tell(sender, "Define Player");
				return true;
			}
		} else if (sender.hasPermission("scorch.command.ping.others")) {
			target = Bukkit.getPlayer(args[0]);
		}

		if (target == null) {
			MSG.tell(sender, "Unknown Player");
			return true;
		}

		int ping = 0;

		try {
			Object entityPlayer = target.getClass().getMethod("getHandle").invoke(target);
			ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | NoSuchFieldException e) {
			e.printStackTrace();
		}

		MSG.tell(sender, "&7" + target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s")
				+ " ping is: " + MSG.getPingColor(ping) + ping);

		return true;
	}
}
