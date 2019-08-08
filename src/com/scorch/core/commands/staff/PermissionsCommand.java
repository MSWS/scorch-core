package com.scorch.core.commands.staff;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

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
					pp.setGroup(pg);
					MSG.tell(sender, "Successfully set " + target.getName() + "'s group to " + pg.getGroupName());
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
			if (args.length < 2) {
				sendGroupHelp(sender, label);
				return true;
			}

			if (args[1].equalsIgnoreCase("list")) {
				MSG.tell(sender, "Listing Available Groups");
				perms.getGroupList().forEach(g -> MSG.tell(sender, g.getGroupName()));
				return true;
			}

			if (args.length < 3) {
				sendGroupHelp(sender, label);
				return true;
			}

			PermissionGroup pg = perms.getGroup(args[1]);

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
						MSG.tell(sender, "Unknown group: " + args[3]);
						return true;
					}
					ng.addInheritedGroup(ng);
				}

				MSG.tell(sender, perms.addGroup(ng) ? "Successfully created new group" : "Unable to create group");
				break;
			case "delete":
				if (!sender.hasPermission("scorch.command.permissions.group.delete." + args[1])) {
					MSG.tell(sender, getPermissionMessage());
					return true;
				}
				if (pg == null) {
					MSG.tell(sender, "Unknown group");
					return true;
				}
				MSG.tell(sender, perms.removeGroup(args[1]) ? "Successfully deleted group" : "Unable to delete group");
				break;
			case "set":
				if (args.length < 5) {
					sendGroupHelp(sender, label);
					return true;
				}
				if (pg == null) {
					MSG.tell(sender, "Unknown group");
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
					MSG.tell(sender, "Set " + pg.getGroupName() + "'s prefix to " + prefix);
					break;
				case "default":
					if (!sender.hasPermission("scorch.command.permissions.group." + args[1] + ".default")) {
						MSG.tell(sender, getPermissionMessage());
						return true;
					}
					boolean val = false;
					try {
						val = Boolean.getBoolean(args[4]);
					} catch (IllegalArgumentException e) {
						MSG.tell(sender, "Must be true or false");
						return true;
					}
					pg.setDefault(val);
					MSG.tell(sender, "Set " + pg.getGroupName() + " default to " + MSG.TorF(val));
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
					MSG.tell(sender, "Unknown group");
					return true;
				}

				ng = perms.getGroup(args[4]);
				if (ng == null) {
					MSG.tell(sender, "Unknown group: " + args[4]);
					return true;
				}

				switch (args[3].toLowerCase()) {
				case "set":
					pg.setInheritedGroups(Arrays.asList(args[4]));
					MSG.tell(sender, "Set " + pg.getGroupName() + "'s parent to " + ng.getGroupName());
					break;
				case "add":
					MSG.tell(sender,
							pg.addInheritedGroup(ng)
									? "Added " + ng.getGroupName() + " to " + pg.getGroupName() + "'s parents"
									: "Unable to add inheritance");
					break;
				case "remove":
					MSG.tell(sender,
							pg.removeInheritedGroup(ng)
									? "Removed " + ng.getGroupName() + " to " + pg.getGroupName() + "'s parents"
									: "Unable to remove inheritance");
					break;
				}
				break;
			}

			return true;
		default:
			MSG.tell(sender, "Unknown arguments.");
			break;
		}

		return true;
	}

	public void sendGroupHelp(CommandSender sender, String label) {
		MSG.tell(sender, "/" + label + " group list");
		MSG.tell(sender, "/" + label + " group [Name] create <Parent>");
		MSG.tell(sender, "/" + label + " group [Name] delete");
		MSG.tell(sender, "/" + label + " group [Name] set [priority/prefix/default] [value]");
		MSG.tell(sender, "/" + label + " group [Name] parents [set/add/remove] [Group]");
	}

	public void sendPlayerHelp(CommandSender sender, String label) {
		MSG.tell(sender, "/" + label + " user [Name] group [set/add/remove] [Group]");
		MSG.tell(sender, "/" + label + " user [Name]");
		MSG.tell(sender, "/" + label + " user [Name] [add/remove] [perm]");
	}

}
