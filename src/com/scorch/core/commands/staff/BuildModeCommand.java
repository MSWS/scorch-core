package com.scorch.core.commands.staff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.staff.BuildModeModule;
import com.scorch.core.modules.staff.BuildModeModule.BuildStatus;
import com.scorch.core.utils.MSG;

/**
 * Build Mode command, ability to modify the lobby/server physically, however
 * changes are logged and can be rolled back by an admin<br>
 * <i>Aliases: bm, build</i>
 * 
 * <b>Permissions</b><br>
 * scorch.command.buildmode - Access to /buildmode <br>
 * scorch.command.buildmode.rollback - Access to rollback builds via /bm
 * rollback<br>
 * scorch.command.buildmode.others - Access to specify a player<br>
 * scorch.command.buildmode.override - Access to enable override mode via /bm
 * override<br>
 * scorch.command.buildmode.inspect - Access to enable inspection mode via /bm
 * inspect
 * 
 * @author imodm
 *
 */
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
		} else if (args.length >= 2 && sender.hasPermission("scorch.command.buildmode.others")) {
			target = Bukkit.getPlayer(args[1]);
		} else {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else if (args.length == 1) {
				target = Bukkit.getPlayer(args[0]);
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

					MSG.cTell(sender, "buildmoderollbackall");
					return true;
				}

				if (target == null) {
					// bm rollback <Player>
					// bm rollback [Player] [number]
					// bm rollback [number]

					if (!NumberUtils.isNumber(args[1])) {
						MSG.tell(sender, "Unknown Player");
						return true;
					}

					if (!(sender instanceof Player)) {
						MSG.tell(sender, "Specify Player");
						return true;
					}

					target = (Player) sender;

					int num = Integer.parseInt(args[1]);
					if (!bm.rollback(target.getUniqueId(), num)) {
						msg = ScorchCore.getInstance().getMessage("buildmoderollbackfail")
								.replace("%player%", target.getName()).replace("%block%", num + "")
								.replace("%s%", num == 1 ? "" : "s");
						MSG.tell(sender, msg);
						return true;
					}
					msg = ScorchCore.getInstance().getMessage("buildmoderollbacknumber")
							.replace("%player%", target.getName())
							.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s")
							.replace("%block%", num + "").replace("%bs%", num == 1 ? "" : "s");
					MSG.tell(sender, msg);
					return true;
				}

				if (args.length > 2 && args.length == 3 && NumberUtils.isNumber(args[2])) {
					int num = Integer.parseInt(args[2]);
					if (!bm.rollback(target.getUniqueId(), num)) {
						msg = ScorchCore.getInstance().getMessage("buildmoderollbackfail")
								.replace("%player%", target.getName()).replace("%block%", num + "")
								.replace("%s%", num == 1 ? "" : "s");
						MSG.tell(sender, msg);
						return true;
					}
					msg = ScorchCore.getInstance().getMessage("buildmoderollbacknumber")
							.replace("%player%", target.getName())
							.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s")
							.replace("%block%", num + "").replace("%bs%", num == 1 ? "" : "s");
					MSG.tell(sender, msg);
					return true;
				}

				bm.rollback(target.getUniqueId(), false);
				msg = ScorchCore.getInstance().getMessage("buildmoderollback").replace("%player%", target.getName())
						.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s");
				MSG.tell(sender, msg);
				return true;
			case "inspect":
				if (!sender.hasPermission("scorch.command.buildmode.inspect")) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				msg = ScorchCore.getInstance().getMessage("buildmodetoggle").replace("%player%", target.getName())
						.replace("%status%",
								bm.toggleMode(target.getUniqueId(), BuildStatus.INSPECT) ? "&aenabled" : "&cdisabled")
						.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s")
						.replace("%mode%", "inspection");

				MSG.tell(sender, msg);
				return true;
			case "override":
				if (!sender.hasPermission("scorch.command.buildmode.override")) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				msg = ScorchCore.getInstance().getMessage("buildmodetoggle").replace("%player%", target.getName())
						.replace("%status%",
								bm.toggleMode(target.getUniqueId(), BuildStatus.OVERRIDE) ? "&aenabled" : "&cdisabled")
						.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s")
						.replace("%mode%", "override");

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

		if (args.length == 1) {
			target = Bukkit.getPlayer(args[0]);
		}

		if (target == null) {
			MSG.tell(sender, "Unknown Player");
			return true;
		}

		msg = ScorchCore.getInstance().getMessage("buildmodetoggle").replace("%player%", target.getName())
				.replace("%status%",
						bm.toggleMode(target.getUniqueId(), BuildStatus.BUILD) ? "&aenabled" : "&cdisabled")
				.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s").replace("%mode%", "build");

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
