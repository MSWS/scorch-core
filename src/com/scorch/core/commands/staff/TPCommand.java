package com.scorch.core.commands.staff;

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
import com.scorch.core.modules.staff.TeleportModule;
import com.scorch.core.utils.MSG;

/**
 * Teleport command meant for general moderation or admins
 * 
 * <b>Permissions</b><br>
 * scorch.command.teleport - Access to command<br>
 * scorch.command.teleport.history - Access to view teleport history of self<br>
 * scorch.command.teleport.back - Access to teleport to previous teleport
 * locations<br>
 * scorch.command.teleport.back.others - Access to force other players to
 * teleport to previous teleport locations
 * 
 * @author imodm
 *
 */
public class TPCommand extends BukkitCommand {

	private TeleportModule module = null;

	public TPCommand(String name) {
		super(name);
		setPermission("scorch.command.tp");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("tp"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (module == null) {
			module = (TeleportModule) ScorchCore.getInstance().getModule("TeleportModule");
		}

		if (args.length == 0) {
			MSG.tell(sender, "/tp [subject] [target]");
			return true;
		}

		List<Player> players = Arrays.asList(Bukkit.getPlayer(args[0]));
		Player tmp = null;
		Location target = null;
		String playerName = args[0], targetName = "Unknown";

		if (args[0].equalsIgnoreCase("history") && sender.hasPermission("scorch.command.teleport.history")) {
			if (args.length == 1) {
				if (sender instanceof Player) {
					tmp = (Player) sender;
				} else {
					MSG.tell(sender, "Specify Player.");
					return true;
				}
			} else {
				tmp = Bukkit.getPlayer(args[1]);
			}

			if (tmp == null) {
				MSG.tell(sender, "Unknown Player.");
				return true;
			}

			List<Location> hist = module.getRecentTeleports(tmp);

			if (hist.isEmpty()) {
				MSG.tell(sender, "&a" + tmp.getName() + " &7has no teleport history");
				return true;
			}

			MSG.tell(sender, "&a" + tmp.getName() + "&7 has &e" + hist.size() + "&7 entr"
					+ (hist.size() == 1 ? "y" : "ies") + " in their teleport history");
			for (int i = 0; i < hist.size() && i < 10; i++) {
				MSG.tell(sender, "&e" + (i + 1) + "&7: [&8" + hist.get(i).getWorld().getName() + "&7] &a"
						+ hist.get(i).getBlockX() + " " + hist.get(i).getBlockY() + " " + hist.get(i).getBlockZ());
			}
			return true;
		} else if (args[0].matches("(?i)(back|b)") && sender.hasPermission("scorch.command.teleport.back")) {
			if (args.length == 1) {
				if (sender instanceof Player) {
					int index = 0;

					if (index >= module.getRecentTeleports((Player) sender).size()) {
						MSG.tell(sender, "&e" + sender.getName() + "&7 has no remaining teleport history positions.");
						return true;
					}
					target = module.getRecentTeleports((Player) sender).get(index);
					targetName = "previous teleport location";
					playerName = sender.getName();
					players.set(0, (Player) sender);
				} else {
					MSG.tell(sender, "Specify player");
					return true;
				}
			} else if (args.length == 2) {
				tmp = Bukkit.getPlayer(args[1]);
				if (tmp == null) {
					if (sender instanceof Player) {
						int index = 0;
						try {
							index = Integer.parseInt(args[1]) - 1;
						} catch (NumberFormatException e) {
							MSG.tell(sender, "Unknown format of number");
							return true;
						}

						if (index >= module.getRecentTeleports((Player) sender).size()) {
							MSG.tell(sender,
									"&e" + sender.getName() + "&7 has no remaining teleport history positions.");
							return true;
						}

						target = module.getRecentTeleports((Player) sender).get(index);
						targetName = index + 1 + " teleport location" + (index + 1 == 1 ? "" : "s") + " back";
						playerName = sender.getName();
						players.set(0, (Player) sender);
					} else {
						MSG.tell(sender, "Specify Player");
						return true;
					}
				} else if (sender.hasPermission("scorch.command.teleport.back.others")) {
					if (module.getRecentTeleports(tmp).isEmpty()) {
						MSG.tell(sender, "&e" + tmp.getName() + "&7 has no remaining teleport history positions.");
						return true;
					}
					target = module.getRecentTeleports(tmp).get(0);
					players.set(0, tmp);
					targetName = "last teleport location";
					playerName = tmp.getName();
				}
			} else if (args.length == 3) {
				tmp = Bukkit.getPlayer(args[1]);
				int index = 0;
				try {
					index = Integer.parseInt(args[2]) - 1;
				} catch (NumberFormatException e) {
					MSG.tell(sender, "Unknown format of number");
					return true;
				}

				if (index >= module.getRecentTeleports(tmp).size()) {
					MSG.tell(sender, "No other teleport history");
					return true;
				}

				target = module.getRecentTeleports(tmp).get(index);
				targetName = (index + 1) + " teleport location" + (index + 1 == 1 ? "" : "s") + " back";
				playerName = tmp.getName();
				players.set(0, tmp);
			}
		} else if (args[0].equalsIgnoreCase("all") && sender.hasPermission("scorch.command.teleport.all")) {
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
				MSG.tell(sender, "Unknown Player");
				return true;
			}

			playerName = "everyone";
		} else {
			if (players.get(0) != null) {
				playerName = players.get(0).getName();
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
							MSG.tell(sender, "Unknown Player");
							return true;
						}
					} else {
						target = tmp.getLocation();
						targetName = tmp.getName();
					}
				}
			} else {
				if (args.length >= 3 && NumberUtils.isNumber(args[0])) {
					if (!(sender instanceof Player)) {
						MSG.tell(sender, "Unknown Player");
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
			for (String res : new String[] { "all", "back", "history", "b" }) {
				if (res.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(res);
				}
			}

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(p.getName());
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
