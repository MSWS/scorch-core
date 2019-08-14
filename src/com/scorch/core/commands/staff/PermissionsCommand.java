package com.scorch.core.commands.staff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.permissions.PermissionGroup;
import com.scorch.core.modules.permissions.PermissionModule;
import com.scorch.core.modules.permissions.PermissionPlayer;
import com.scorch.core.utils.MSG;

public class PermissionsCommand extends BukkitCommand {

	public PermissionsCommand(String name) {
		super(name);
		setPermission("scorch.command.permissions");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("perms"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		PermissionModule perms = ScorchCore.getInstance().getPermissionModule();

		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length == 0) {
			sendGroupHelp(sender, label);
			sendPlayerHelp(sender, label);

			return true;
		}

		switch (args[0].toLowerCase()) {
		case "user":
			MSG.tell(sender, " ");

			if (args.length < 2) {
				sendPlayerHelp(sender, label);
				return true;
			}

			OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

			if (!sender.hasPermission("scorch.command.permissions.manage." + args[1])) {
				MSG.tell(sender, getPermissionMessage());
				return true;
			}

			PermissionPlayer pp = perms.getPermissionPlayer(target.getUniqueId());

			if (args.length == 2) {
				pp.getPermissions().forEach(perm -> MSG.tell(sender, perm));
				MSG.tell(sender, target.getName() + " is a member of the following group(s): " + pp.getGroupNames());
				return true;
			}

			String perm;

			switch (args[2].toLowerCase()) {
			case "add":
				if (!sender.hasPermission("scorch.command.permissions.add." + args[1])) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				if (args.length < 4) {
					sendPlayerHelp(sender, label);
					return true;
				}
				perm = args[3];
				if (pp.hasPermission(perm)) {
					MSG.tell(sender, target.getName() + " already has " + perm);
					return true;
				}
				MSG.tell(sender, pp.addPermission(perm) ? "Successfully added permission" : "Unable to add " + perm);
				break;
			case "remove":
				if (!sender.hasPermission("scorch.command.permissions.remove." + args[1])) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				if (args.length < 4) {
					sendPlayerHelp(sender, label);
					return true;
				}
				perm = args[3];
				if (!pp.hasPermission(perm)) {
					MSG.tell(sender, target.getName() + " doesn't have " + perm);
					return true;
				}
				MSG.tell(sender,
						pp.removePermission(perm) ? "Successfully removed permission" : "Unable to remove " + perm);
				break;
			case "group":
				if (!sender.hasPermission("scorch.command.permissions.group." + args[1])) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				if (args.length < 5) {
					sendPlayerHelp(sender, label);
					return true;
				}
				String group = args[4];
				PermissionGroup pg = perms.getGroup(group);

				if (pg == null) {
					MSG.tell(sender, "Unknown group.");
					return true;
				}

				switch (args[3].toLowerCase()) {
				case "set":
					MSG.tell(sender,
							pp.setGroup(pg)
									? "Successfully set " + target.getName() + "'s group to " + pg.getGroupName()
									: "Unable to set group");
					break;
				case "add":
					MSG.tell(sender, pp.addGroup(pg) ? "Success" : "Unable to add group");
					break;
				case "remove":
					MSG.tell(sender, pp.removeGroup(pg) ? "Success" : "Unable to remove group");
					break;
				}
				break;
			default:
				MSG.tell(sender, "Unknown arguments.");
				break;
			}

			return true;
		case "group":
			MSG.tell(sender, " ");
			if (args.length < 2 || args[1].equalsIgnoreCase("list")) {
				if (perms.getGroupList().isEmpty()) {
					MSG.tell(sender, "&cThere are no permission groups.");
					return true;
				}

				MSG.tell(sender, "&aListing Available Groups");

				for (PermissionGroup g : perms.getGroupList()) {
					String msg = "&e" + g.getGroupName() + "&7";
					if (g.getInheritedGroups().size() > 0)
						msg += " parents " + g.getInheritedGroupNames();
					if (!g.getPrefix().isEmpty())
						msg += " Prefix: " + g.getPrefix().trim();
					msg += " Weight: " + g.getWeight();

					MSG.tell(sender, msg);
				}
				return true;
			}

			PermissionGroup pg = perms.getGroup(args[1]);

			if (args.length < 3) {
				if (pg == null) {
					MSG.tell(sender, "&cUnknown permission group: &e" + args[1]);
					return true;
				}
				MSG.tell(sender, "&e" + pg.getGroupName()
						+ (pg.getPrefix().isEmpty() ? "" : " &7(" + pg.getPrefix().trim() + "&7) &r")
						+ " &7contains the following permission" + (pg.getPermissions().size() == 1 ? "" : "s") + ":");
				for (String p : pg.getPermissions()) {
					MSG.tell(sender, "- " + p + (sender.hasPermission(p) ? " (Owned)" : ""));
				}
				return true;
			}

			switch (args[2].toLowerCase()) {
			case "create":
				if (!sender.hasPermission("scorch.command.permissions.group.create")) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				PermissionGroup ng = new PermissionGroup(args[1], false, null, 0, new ArrayList<String>());
				if (args.length > 3) {
					pg = perms.getGroup(args[3]);
					if (pg == null) {
						MSG.tell(sender, "&cUnknown permission group: &e" + args[1]);
						return true;
					}
					ng.addInheritedGroup(ng);
				}

				MSG.tell(sender, perms.addGroup(ng) ? "&aSuccessfully created group &e" + args[1] + "&7."
						: "&cUnable to create group.");
				break;
			case "delete":
				if (!sender.hasPermission("scorch.command.permissions.group.delete." + args[1])) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				if (pg == null) {
					MSG.tell(sender, "&cUnknown permission group: &e" + args[1]);
					return true;
				}
				MSG.tell(sender, perms.removeGroup(args[1]) ? "&aSuccessfully &6deleted &e" + args[1] + "&7."
						: "&cUnable to delete group.");
				break;
			case "add":
				if (!sender.hasPermission("scorch.command.permissions.group.add." + args[1])) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}

				if (pg == null) {
					MSG.tell(sender, "&cUnknown permission group: &e" + args[1]);
					return true;
				}

				if (args.length < 4) {
					sendGroupHelp(sender, label);
					return true;
				}

				perm = args[3];
				MSG.tell(sender,
						pg.addPermission(perm)
								? "&aSuccessfully &6added &e" + perm + "&7 to &a" + MSG.plural(pg.getGroupName())
										+ " &7permisisons list."
								: "Unable to add " + perm + " to " + pg.getGroupName());
				break;
			case "remove":
				if (!sender.hasPermission("scorch.command.permissions.group.remove." + args[1])) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}

				if (pg == null) {
					MSG.tell(sender, "&cUnknown permission group: &e" + args[1]);
					return true;
				}

				if (args.length < 4) {
					sendGroupHelp(sender, label);
					return true;
				}

				perm = args[3];
				MSG.tell(sender,
						pg.removePermission(perm)
								? "&cRemoved &e" + perm + " &7from &a" + MSG.plural(pg.getGroupName())
										+ " &7permissions."
								: "&cUnable to remove &e" + perm + "&7 from &a" + pg.getGroupName() + "&7.");
				break;
			case "set":
				if (args.length < 5) {
					sendGroupHelp(sender, label);
					return true;
				}
				if (pg == null) {
					MSG.tell(sender, "&cUnknown permission group: &e" + args[1]);
					return true;
				}

				switch (args[3].toLowerCase()) {
				case "priority":
					if (!sender.hasPermission("scorch.command.permissions.group." + args[1] + ".priority")) {
						MSG.tell(sender, getPermissionMessage());
						return true;
					}
					if (!NumberUtils.isDigits(args[4])) {
						MSG.tell(sender, "Invalid priority");
						return true;
					}
					pg.setWeight(Integer.valueOf(args[4]));
					MSG.tell(sender, "&aSuccessfully &7set &e" + MSG.plural(pg.getGroupName()) + " &6priority &7to &a"
							+ args[4] + "&7.");
					break;
				case "prefix":
					if (!sender.hasPermission("scorch.command.permissions.group." + args[1] + ".prefix")) {
						MSG.tell(sender, getPermissionMessage());
						return true;
					}
					String prefix = "";
					for (int i = 4; i < args.length; i++)
						prefix += args[i] + " ";
					pg.setPrefix(prefix);
					MSG.tell(sender, "&aSuccessfully &7set &e" + MSG.plural(pg.getGroupName()) + " &6prefix &7to &r"
							+ prefix + "&7.");
					break;
				case "default":
					if (!sender.hasPermission("scorch.command.permissions.group." + args[1] + ".default")) {
						MSG.tell(sender, getPermissionMessage());
						return true;
					}
					boolean val = false;
					try {
						val = Boolean.valueOf(args[4]);
					} catch (IllegalArgumentException e) {
						MSG.tell(sender, "&cMust be true or false");
						return true;
					}
					pg.setDefault(val);
					MSG.tell(sender,
							"&7Set " + MSG.plural(pg.getGroupName()) + " default status to " + MSG.TorF(val) + "&7.");
					break;
				}
				break;
			case "parents":
				if (!sender.hasPermission("scorch.command.permissions.group." + args[1] + ".parents")) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				if (args.length < 5) {
					sendGroupHelp(sender, label);
					return true;
				}

				if (pg == null) {
					MSG.tell(sender, "&cUnknown permission group: &e" + args[1]);
					return true;
				}

				ng = perms.getGroup(args[4]);
				if (ng == null) {
					MSG.tell(sender, "&cUnknown permission group: &e" + args[1]);
					return true;
				}

				switch (args[3].toLowerCase()) {
				case "set":
					pg.setInheritedGroups(Arrays.asList(args[4]));
					MSG.tell(sender, "&aSuccessfully &7set &e" + MSG.plural(pg.getGroupName()) + " &6parent &7to &a"
							+ ng.getGroupName() + "&7.");
					break;
				case "add":
					MSG.tell(sender,
							pg.addInheritedGroup(ng)
									? "&aAdded &e" + ng.getGroupName() + "&7 to &a" + MSG.plural(pg.getGroupName())
											+ " &6parents &7list."
									: "&cUnable to add inheritance");
					break;
				case "remove":
					MSG.tell(sender,
							pg.removeInheritedGroup(ng)
									? "&cRemoved &e" + ng.getGroupName() + " from &a" + MSG.plural(pg.getGroupName())
											+ " &6parents&7."
									: "Unable to remove inheritance");
					break;
				default:
					MSG.tell(sender, "&cUnknown arguments.");
					break;
				}
			default:
				MSG.tell(sender, "&cUnknown arguments.");
				break;
			}
			break;
		default:
			MSG.tell(sender, "&cUnknown arguments.");
			break;
		}

		return true;
	}

	public void sendGroupHelp(CommandSender sender, String label) {
		MSG.tell(sender, "/" + label + " group");
		MSG.tell(sender, "/" + label + " group list");
		MSG.tell(sender, "/" + label + " group [Name] create <Parent>");
		MSG.tell(sender, "/" + label + " group [Name] delete");
		MSG.tell(sender, "/" + label + " group [Name] set [priority/prefix/default] [value]");
		MSG.tell(sender, "/" + label + " group [Name] parents [set/add/remove] [Group]");
	}

	public void sendPlayerHelp(CommandSender sender, String label) {
		MSG.tell(sender, "/" + label + " user [Name]");
		MSG.tell(sender, "/" + label + " user [Name] [add/remove] [perm]");
		MSG.tell(sender, "/" + label + " user [Name] group [set/add/remove] [Group]");
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<>();
		if (args.length <= 1) {
			for (String res : new String[] { "user", "group" })
				if (res.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
					result.add(res);
			return result;
		}

		if (args[0].equalsIgnoreCase("user")) {
			switch (args.length) {
			case 2:
				for (Player p : Bukkit.getOnlinePlayers())
					if (p.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
						result.add(p.getName());
				break;
			case 3:
				for (String res : new String[] { "group", "add", "remove" })
					if (res.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
						result.add(res);

				break;
			case 4:
				if (args[2].equalsIgnoreCase("group")) {
					for (String res : new String[] { "set", "add", "remove" })
						if (res.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
							result.add(res);
				}
				if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove")) {
					PermissionPlayer pp = ScorchCore.getInstance().getPermissionModule()
							.getPermissionPlayer(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
					for (String perm : pp.getPermissions())
						if (perm.toLowerCase().startsWith(args[args.length - 1]))
							result.add(perm);
				}
				break;
			case 5:
				if (!args[2].equalsIgnoreCase("group"))
					break;
				for (PermissionGroup group : ScorchCore.getInstance().getPermissionModule().getGroupList())
					if (group.getGroupName().toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
						result.add(group.getGroupName());
				break;
			}
		} else if (args[0].equalsIgnoreCase("group")) {
			switch (args.length) {
			case 2:
				if ("list".toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
					result.add("list");

			case 4:
				if (args.length >= 3 && args[2].equalsIgnoreCase("parents")) {
					for (String res : new String[] { "set", "add", "remove" })
						if (res.toLowerCase().startsWith(args[args.length - 1]))
							result.add(res);
					break;
				}
				if (args.length > 2)
					if (args[2].equalsIgnoreCase("set"))
						for (String res : new String[] { "prefix", "parents", "priority", "default" })
							if (res.toLowerCase().startsWith(args[args.length - 1]))
								result.add(res);

				if (args[args.length - 1].equalsIgnoreCase("delete"))
					break;
			case 5:
				for (PermissionGroup group : ScorchCore.getInstance().getPermissionModule().getGroupList())
					if (group.getGroupName().toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
						result.add(group.getGroupName());
				if (args.length > 3)
					if (args[2].equalsIgnoreCase("set") && args[3].equalsIgnoreCase("default")) {
						for (String res : new String[] { "true", "false" }) {
							if (res.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
								result.add(res);
						}
					}
				break;
			case 3:
				for (String res : new String[] { "set", "create", "delete", "parents" })
					if (res.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(res);

				break;
			}
		}

		return result;
	}

}
