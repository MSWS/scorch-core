package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.CPlayer;
import com.scorch.core.modules.staff.BuildModeModule;
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
		} else if (sender.hasPermission("scorch.command.buildmode.others")) {
			target = Bukkit.getPlayer(args[0]);
		} else {
			MSG.tell(sender, ScorchCore.getInstance().getMessage("noperm"));
			return true;
		}

		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
			case "revert":
			case "reset":
				if (args.length < 2) {
					MSG.tell(sender, "/buildmode " + args[0] + " [Player]");
					return true;
				}

				target = Bukkit.getPlayer(args[1]);

				bm.rollback(target.getUniqueId());
				MSG.tell(sender, "Reverted " + target.getName() + "'"
						+ (target.getName().toLowerCase().endsWith("s") ? "" : "s") + " builds");
				return true;
			case "inspect":
				CPlayer cp = ScorchCore.getInstance().getPlayer((Player) sender);
				if (cp.hasTempData("buildModeInspection")) {
					cp.removeTempData("buildModeInspection");
					MSG.tell(sender, "Inspection mode disabled");
				} else {
					cp.setTempData("buildModeInspection", true);
					MSG.tell(sender, "Inspection mode enabled");
				}
				return true;
			default:
				MSG.tell(sender, "Unknown arguments");
				return true;
			}
		}

		String msg = ScorchCore.getInstance().getMessage("buildmodetoggle").replace("%player%", target.getName())
				.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s")
				.replace("%status%", bm.toggleBuildMode(target.getUniqueId()) ? "&aenabled" : "&cdisabled");

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
			for (String res : new String[] { "revert", "inspect" }) {
				if (res.toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(res);
			}
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("revert")) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args[1].toLowerCase()))
					result.add(p.getName());
			}
		}

		return result;
	}

}
