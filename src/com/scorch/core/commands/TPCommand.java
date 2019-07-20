package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

public class TPCommand extends BukkitCommand {

	public TPCommand(String name) {
		super(name);
		setPermission("scorch.command.tp");
		setPermissionMessage(ScorchCore.getInstance().getMessages().getMessage("noperm").getMessage());
		setAliases(Arrays.asList("tp"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length == 0) {
			MSG.tell(sender, "/tp [subject] [target]");
			return true;
		}

		List<Player> players = Arrays.asList(Bukkit.getPlayer(args[0]));
		Player tmp = null;
		Location target = null;
		String playerName = args[0], targetName = "Unknown";

		if (args[0].equalsIgnoreCase("all")) {
			players = Bukkit.getOnlinePlayers().stream().collect(Collectors.toList());
			if (args.length == 1 && sender instanceof Player) {
				target = ((Player) sender).getLocation();
				targetName = sender.getName();
			} else if (args.length == 2) {
				target = Bukkit.getPlayer(args[1]).getLocation();
				targetName = Bukkit.getPlayer(args[1]).getName();
			} else if (args.length >= 4) {
				target = new Location(((Player) sender).getWorld(), Double.parseDouble(args[1]) + .5,
						Double.parseDouble(args[2]) + .5, Double.parseDouble(args[3]) + .5);
				targetName = String.format("%s %s %s", args[1], args[2], args[3]);
				if (args.length == 6) {
					target.setYaw(Float.parseFloat(args[4]));
					target.setPitch(Float.parseFloat(args[5]));
					targetName = String.format("%s %s %s %s %s", args[1], args[2], args[3], args[4], args[5]);
				}

			} else {
				MSG.tell(sender, "unknown target");
				return true;
			}

			playerName = "everyone";
		} else {
			if (players.get(0) != null) {
				if (args.length == 1) {
					if (sender instanceof Player) {
						target = players.get(0).getLocation();
						targetName = players.get(0).getName();
						players.set(0, (Player) sender);
						playerName = sender.getName();
					} else {
						MSG.tell(sender, "define target");
						return true;
					}
				} else {
					tmp = Bukkit.getPlayer(args[1]);
					if (tmp == null) {
						if (NumberUtils.isNumber(args[1]) && args.length >= 4) {
							target = new Location(players.get(0).getWorld(), Double.parseDouble(args[1]),
									Double.parseDouble(args[2]), Double.parseDouble(args[3]));
							targetName = String.format("%s %s %s", args[1], args[2], args[3]);
							if (args.length == 6) {
								target.setYaw(Float.parseFloat(args[4]));
								target.setPitch(Float.parseFloat(args[5]));
								targetName = String.format("%s %s %s %s %s", args[1], args[2], args[3], args[4],
										args[5]);
							}
						} else {
							MSG.tell(sender, "unknown player");
							return true;
						}
					} else {
						target = tmp.getLocation();
						targetName = tmp.getName();
					}
				}
			} else {
				if (args.length == 3 && NumberUtils.isNumber(args[0])) {
					if (!(sender instanceof Player)) {
						MSG.tell(sender, "unknown player");
						return true;
					}

					players.set(0, (Player) sender);
					playerName = sender.getName();

					target = new Location(((Player) sender).getWorld(), Double.parseDouble(args[0]) + .5,
							Double.parseDouble(args[1]) + .5, Double.parseDouble(args[2]) + .5);
					targetName = String.format("%s %s %s", args[0], args[1], args[2]);
					if (args.length == 5) {
						target.setYaw(Float.parseFloat(args[3]));
						target.setPitch(Float.parseFloat(args[4]));
						targetName = String.format("%s %s %s %s %s", args[0], args[1], args[2], args[3], args[4]);
					}
				}
			}
		}

		MSG.tell(sender, "&7Teleported &e" + playerName + " &7to &e" + targetName);
		for (Player p : players)
			p.teleport(target);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();
		if (args.length <= 1) {
			for (String res : new String[] { "all" }) {
				if (res.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(res);
				}
			}
		}

		if (args.length == 2) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
					result.add(p.getName());
				}
			}
		}

		return result;
	}

}
