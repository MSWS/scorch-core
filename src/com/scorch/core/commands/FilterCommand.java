package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.chat.FilterEntry;
import com.scorch.core.modules.chat.FilterEntry.FilterType;
import com.scorch.core.modules.chat.FilterModule;
import com.scorch.core.modules.players.CPlayer;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.MSG;

/**
 * Manage and update the filter
 * 
 * <b>Permissions</b><br>
 * 
 * @author imodm
 *
 */
public class FilterCommand extends BukkitCommand {
	public FilterCommand(String name) {
		super(name);
		setPermission("scorch.command.filter");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length == 0) {
			MSG.tell(sender, "Invalid arguments");
			return true;
		}

		FilterModule fm = (FilterModule) ScorchCore.getInstance().getModule("FilterModule");
		FilterEntry entry;

		String word;

		Player player;

		switch (args[0].toLowerCase()) {
		case "preference":
			if (!sender.hasPermission("scorch.command.filter.preference")) {
				MSG.cTell(sender, "noperm");
				return true;
			}
			if (!(sender instanceof Player)) {
				MSG.tell(sender, "You must be a player");
				return true;
			}
			player = (Player) sender;
			ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(player.getUniqueId());
			if (args.length == 1) {
				MSG.tell(sender, "/filter preference NONE/REGULAR");
				return true;
			}
			if (args[1].equalsIgnoreCase("none")) {
				sp.setData("filterpreference", "NONE");
			} else if (args[1].equalsIgnoreCase("regular")) {
				sp.setData("filterpreference", "REGULAR");
			}
			break;
		case "enable":
			if (!sender.hasPermission("scorch.command.filter.enable")) {
				MSG.cTell(sender, "noperm");
				return true;
			}
			if (fm.isEnabled()) {
				MSG.tell(sender, "Filter is already enabled");
				return true;
			}

			fm.initialize();
			break;
		case "disable":
			if (!sender.hasPermission("scorch.command.filter.disable")) {
				MSG.cTell(sender, "noperm");
				return true;
			}
			if (!fm.isEnabled()) {
				MSG.tell(sender, "Filter is already disabled");
				return true;
			}

			fm.disable();
			MSG.tell(sender, "Filter disabled.");
			break;
		case "addword":
			if (!sender.hasPermission("scorch.command.filter.addword")) {
				MSG.cTell(sender, "noperm");
				return true;
			}
			FilterType type = FilterType.REGULAR;
			boolean defined = false;
			if (args.length >= 3) {
				try {
					type = FilterType.valueOf(args[args.length - 1].toUpperCase());
					defined = true;
				} catch (IllegalArgumentException expected) {
				}
			}

			word = "";
			for (int i = 1; i < args.length - (defined ? 1 : 0); i++) {
				word += args[i] + " ";
			}

			word = word.trim();

			entry = new FilterEntry(word, type);
			fm.addWord(entry);

			MSG.tell(sender, "Added word " + entry.getWord() + " with level of " + entry.getType());
			break;
		case "removeword":
			if (!sender.hasPermission("scorch.command.filter.removeword")) {
				MSG.cTell(sender, "noperm");
				return true;
			}
			if (args.length < 2) {
				MSG.tell(sender, "/filter removeword [word]");
				return true;
			}

			word = "";
			for (int i = 1; i < args.length; i++) {
				word += args[i] + " ";
			}

			word = word.trim();

			entry = fm.getFilterEntry(word);
			if (entry == null) {
				MSG.tell(sender, args[1] + " is not filtered");
				return true;
			}

			fm.removeWord(entry);
			MSG.tell(sender, "Removed word " + entry.getWord());
			break;
		case "addbypass":
			if (!sender.hasPermission("scorch.command.filter.addword")) {
				MSG.cTell(sender, "noperm");
				return true;
			}
			if (args.length < 2) {
				MSG.tell(sender, "/filter addbypass [word]");
				return true;
			}
			entry = new FilterEntry(args[1], FilterType.ALLOW);
			fm.addWord(entry);

			MSG.tell(sender, "Added " + entry.getWord() + " to the bypass");
			break;
		case "gui":
			if (!sender.hasPermission("scorch.command.filter.gui")) {
				MSG.cTell(sender, "noperm");
				return true;
			}
			if (!(sender instanceof Player)) {
				MSG.tell(sender, "You must be a player");
				return true;
			}

			player = (Player) sender;
			CPlayer cp = ScorchCore.getInstance().getPlayer(player);

			player.openInventory(fm.getFilterGUI(0));
			cp.setTempData("openInventory", "FilterGUI");
			cp.setTempData("page", 0);
			break;
		}

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();
		if (args.length == 1) {
			for (String res : new String[] { "gui", "preference", "enable", "disable", "addword", "removeword",
					"addbypass" }) {
				if (sender.hasPermission("scorch.command.filter." + res)
						&& res.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(res);
				}
			}
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("preference")) {
			for (String res : new String[] { "NONE", "REGULAR" }) {
				if (res.toLowerCase().startsWith(args[1].toLowerCase())) {
					result.add(res);
				}
			}
		}

		if (args.length >= 3 && args[0].equalsIgnoreCase("addword")) {
			for (FilterType type : FilterType.values()) {
				if (type.toString().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
					result.add(type.toString());
				}
			}
		}

		return result;
	}

}
