package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

/**
 * Basic heal command
 * <li>Sets the player's health
 * <li>Sets the player's satuartion level
 * <li>Sets the player's food level
 * <li>Sets the player's fire ticks
 * 
 * <b>Permissions</b><br>
 * scorch.command.heal - Access to command<br>
 * scorch.command.heal.others - Access to heal others<br>
 * scorch.command.heal.all - Access to heal all players
 * 
 * @author imodm
 *
 */
public class HealCommand extends BukkitCommand {

	public HealCommand(String name) {
		super(name);
		setPermission("scorch.command.heal");
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
		} else if (sender.hasPermission("scorch.command.heal.others")) {
			target = Bukkit.getPlayer(args[0]);
		} else {
			MSG.cTell(sender, "noperm");
		}

		String msg = ScorchCore.getInstance().getMessage("healmessage");

		if (args.length > 0 && args[0].equalsIgnoreCase("all") && sender.hasPermission("scorch.command.heal.all")) {
			for (Player t : Bukkit.getOnlinePlayers()) {
				t.setHealth(t.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
				t.setSaturation(2);
				t.setFoodLevel(20);
				t.setFireTicks(0);

				for (PotionEffect eff : t.getActivePotionEffects())
					t.removePotionEffect(eff.getType());
			}
			MSG.tell(sender, msg.replace("%target%", "everyone"));
			return true;
		}

		if (target == null) {
			MSG.tell(sender, "Unknown Player");
			return true;
		}

		target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		target.setSaturation(2);
		target.setFoodLevel(20);
		target.setFireTicks(0);

		for (PotionEffect eff : target.getActivePotionEffects())
			target.removePotionEffect(eff.getType());

		MSG.tell(sender, msg.replace("%target%", target.getName()));

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();
		if (args.length > 0)
			return result;

		if (sender.hasPermission("scorch.command.heal.all") && "all".toLowerCase().startsWith(args[0].toLowerCase()))
			result.add("all");

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
				result.add(p.getName());
		}

		return result;
	}
	
}
