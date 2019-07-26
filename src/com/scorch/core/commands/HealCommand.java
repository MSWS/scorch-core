package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

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

		MSG.tell(sender, "Healed " + target.getName());

		return true;
	}

}
