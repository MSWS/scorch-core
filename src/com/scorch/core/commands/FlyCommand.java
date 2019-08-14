package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

public class FlyCommand extends BukkitCommand {

	public FlyCommand(String name) {
		super(name);
		setPermission("scorch.command.fly");
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
				MSG.tell(sender, "You must be a player");
				return true;
			}
		} else if (sender.hasPermission("scorch.command.fly.others")) {
			target = Bukkit.getPlayer(args[0]);
		} else {
			MSG.tell(sender, getPermissionMessage());
		}

		boolean allow = !target.getAllowFlight();
		if (!allow)
			target.setFlying(false);

		target.setAllowFlight(allow);
		MSG.tell(sender, (allow ? "&aEnabled" : "&cDisabled") + " &e" + MSG.plural(target.getName()) + " &7Fly Mode.");
		return true;
	}

}
