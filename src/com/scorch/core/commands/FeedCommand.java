package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

/**
 * Basic feed
 * <li>Sets the player's satuartion level
 * <li>Sets the player's food level
 * 
 * <b>Permissions</b><br>
 * scorch.command.feed - Access to command<br>
 * scorch.command.feed.others - Access to feed others<br>
 * scorch.command.feed.all - Access to feed all players
 * 
 * @author imodm
 *
 */
public class FeedCommand extends BukkitCommand {

	public FeedCommand(String name) {
		super(name);
		setPermission("scorch.command.feed");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		Player target = null;
		if (args.length == 0) {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				MSG.tell(sender, "Specify Player");
				return true;
			}
		} else if (sender.hasPermission("scorch.command.feed.others")) {
			target = Bukkit.getPlayer(args[0]);
		} else {
			MSG.cTell(sender, "noperm");
		}

		String msg = ScorchCore.getInstance().getMessage("feedmessage");

		if (args.length > 0 && args[0].equalsIgnoreCase("all") && sender.hasPermission("scorch.command.feed.all")) {
			for (Player t : Bukkit.getOnlinePlayers()) {
				t.setSaturation(2);
				t.setFoodLevel(20);
			}
			MSG.tell(sender, msg.replace("%target%", "everyone"));
			return true;
		}

		if (target == null) {
			MSG.tell(sender, "Unknown Player");
			return true;
		}

		target.setSaturation(2);
		target.setFoodLevel(20);

		MSG.tell(sender, msg.replace("%target%", target.getName()));

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();
		if (args.length > 0)
			return result;

		if (sender.hasPermission("scorch.command.feed.all") && "all".toLowerCase().startsWith(args[0].toLowerCase()))
			result.add("all");

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
				result.add(p.getName());
		}

		return result;
	}
}
