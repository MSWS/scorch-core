package com.scorch.core.commands.staff;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.staff.VanishModule;
import com.scorch.core.utils.MSG;

/**
 * Staff command for moderation
 * 
 * <b>Permissions</b><br>
 * scorch.command.vanish - Access to command <br>
 * scorch.vanish.level.[level] - Used to see who can see who, higher level staff
 * should get higher level numbers (OWNER: 100, CO-OWNER: 99, etc)
 * 
 * @author imodm
 *
 */
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

		String msg = ScorchCore.getInstance().getMessage("togglevanish");

		MSG.tell(sender,
				msg.replace("%status%", vm.toggle(target) ? "&aenabled" : "disabled")
						.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s")
						.replace("%target%", target.getName()));
		return true;
	}
}
