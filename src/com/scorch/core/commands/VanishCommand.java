package com.scorch.core.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.staff.VanishModule;
import com.scorch.core.utils.MSG;

public class VanishCommand extends BukkitCommand {

	private VanishModule vm;

	public VanishCommand(String name) {
		super(name);
		setPermission("scorch.command.vanish");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("v"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (vm == null)
			vm = (VanishModule) ScorchCore.getInstance().getModule("VanishModule");

		if (!(sender.hasPermission(getPermission()))) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		Player target = null;

		if (args.length == 0) {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				MSG.tell(sender, "You must specify a player");
				return true;
			}
		} else if (sender.hasPermission("scorch.command.vanish.others")) {
			target = Bukkit.getPlayer(args[0]);
		}

		if (target == null) {
			MSG.tell(sender, "Unknown Player.");
			return true;
		}

		MSG.tell(sender, "&7You " + (vm.toggle(target) ? "&eenabled" : "&edisabled") + " &7" + target.getName() + "'"
				+ (target.getName().toLowerCase().endsWith("s") ? "" : "s") + " vanish.");
		return true;
	}
}
