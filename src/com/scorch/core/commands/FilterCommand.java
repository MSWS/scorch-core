package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.ModulePriority;
import com.scorch.core.modules.chat.FilterEntry;
import com.scorch.core.modules.chat.FilterModule;
import com.scorch.core.modules.chat.FilterModule.FilterType;
import com.scorch.core.modules.data.ScorchPlayer;
import com.scorch.core.utils.MSG;

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

		switch (args[0].toLowerCase()) {
		case "preference":
			if (!(sender instanceof Player)) {
				MSG.tell(sender, "You must be a player");
				return true;
			}
			Player player = (Player) sender;
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
			if (fm != null) {
				MSG.tell(sender, "Filter is already enabled");
				return true;
			}

			fm = (FilterModule) ScorchCore.getInstance().registerModule(new FilterModule("FilterModule"),
					ModulePriority.MEDIUM);

			fm.initialize();
			break;
		case "disable":
			if (fm == null) {
				MSG.tell(sender, "Filter is already disabled");
				return true;
			}

			ScorchCore.getInstance().disableModule(ScorchCore.getInstance().getModule("FilterModule"));
			MSG.tell(sender, "Filter disabled.");
			break;
		case "addword":
			if (args.length < 3) {
				MSG.tell(sender, "/filter addword [word] [wordtype]");
				return true;
			}

			entry = new FilterEntry(args[1], FilterType.valueOf(args[2].toUpperCase()));
			fm.addWord(entry);
			break;
		case "removeword":
			if (args.length < 2) {
				MSG.tell(sender, "/filter removeword [word]");
				return true;
			}

			entry = fm.getFilterEntry(args[1]);
			if (entry == null) {
				MSG.tell(sender, "Word is not filtered");
				return true;
			}

			fm.removeWord(entry);
			break;
		}

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();
		if (args.length == 1) {
			for (String res : new String[] { "preference", "enable", "disable", "addword" }) {
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

		if (args.length == 3 && args[0].equalsIgnoreCase("addword")) {
			for (FilterType type : FilterType.values()) {
				if (type.toString().toLowerCase().startsWith(args[2].toLowerCase())) {
					result.add(type.toString());
				}
			}
		}

		return result;
	}

}
