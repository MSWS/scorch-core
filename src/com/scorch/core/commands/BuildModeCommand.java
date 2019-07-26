package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.staff.BuildModeModule;
import com.scorch.core.modules.staff.BuildModeModule.BuildStatus;
import com.scorch.core.utils.MSG;

public class BuildModeCommand extends BukkitCommand {

	public BuildModeCommand(String name) {
		super(name);
		setPermission("scorch.command.buildmode");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("bm", "build"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		BuildModeModule bm = ScorchCore.getInstance().getModule("BuildModeModule", BuildModeModule.class);

		Player target;

		if (args.length == 0) {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				MSG.tell(sender, "Specify Player");
				return true;
			}
		} else if (args.length == 2) {
			target = Bukkit.getPlayer(args[1]);
		} else {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				MSG.tell(sender, "Specify Player");
				return true;
			}
		}

		String msg;

		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
			case "revert":
			case "reset":
			case "rollback":
				if (!sender.hasPermission("scorch.command.buildmode.rollback")) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}

				if (args.length == 2 && args[1].equalsIgnoreCase("all")) {
					for (UUID uuid : bm.getBuilders())
						bm.rollback(uuid, false);

					MSG.tell(sender, "Rolled back everyone's builds.");
					return true;
				}

				if (target == null) {
					MSG.tell(sender, "Unknown Player");
					return true;
				}

				bm.rollback(target.getUniqueId(), false);
				MSG.tell(sender, "Rolled back " + target.getName() + "'"
						+ (target.getName().toLowerCase().endsWith("s") ? "" : "s") + " builds");
				return true;
			case "inspect":
				if (!sender.hasPermission("scorch.command.buildmode.inspect")) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				msg = ScorchCore.getInstance().getMessage("buildmodeinspecttoggle")
						.replace("%target%", target.getName())
						.replace("%status%",
								bm.toggleMode(target.getUniqueId(), BuildStatus.INSPECT) ? "&aenabled" : "&cdisabled")
						.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s");

				MSG.tell(sender, msg);
				return true;
			case "override":
				if (!sender.hasPermission("scorch.command.buildmode.override")) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				msg = ScorchCore.getInstance().getMessage("buildmodeoverridetoggle")
						.replace("%target%", target.getName())
						.replace("%status%",
								bm.toggleMode(target.getUniqueId(), BuildStatus.OVERRIDE) ? "&aenabled" : "&cdisabled")
						.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s");

				MSG.tell(sender, msg);
				return true;
			default:
				if (target == null) {
					MSG.tell(sender, "Unknown Player");
					return true;
				}
				break;
			}
		}

		msg = ScorchCore.getInstance().getMessage("buildmodebuildtoggle").replace("%target%", target.getName())
				.replace("%status%",
						bm.toggleMode(target.getUniqueId(), BuildStatus.BUILD) ? "&aenabled" : "&cdisabled")
				.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s");

		MSG.tell(sender, msg);

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();

		if (sender.hasPermission("scorch.command.buildmode.others") && args.length == 1) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(p.getName());
			}
		}

		if (args.length == 1) {
			for (String res : new String[] { "rollback", "inspect", "override" }) {
				if (sender.hasPermission("scorch.command.buildmode." + res))
					if (res.toLowerCase().startsWith(args[0].toLowerCase()))
						result.add(res);
			}
		}

		if (args.length == 2) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args[1].toLowerCase()))
					result.add(p.getName());
			}
			if (args[0].matches("(?i)(revert|reset|rollback)") && "all".toLowerCase().startsWith(args[1].toLowerCase()))
				result.add("all");
		}

		return result;
	}

}
