package com.scorch.core.commands.staff;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.MSG;

public class RecordCommand extends BukkitCommand {

	public RecordCommand(String name) {
		super(name);
		setPermission("scorch.command.record");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("records", "punishments", "staffhistory"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		OfflinePlayer target = null;

		if (args.length == 0) {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				MSG.tell(sender, "You must be a player");
				return true;
			}
		} else if (sender.hasPermission("scorch.command.record.others")) {
			target = Bukkit.getOfflinePlayer(args[0]);
		}

		String name = target.getName() == null ? args[0] : target.getName();

		if (sender instanceof Player) {
			Player player = (Player) sender;
			ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

			sp.setTempData("openInventory", "punishmentrecord");
			sp.setTempData("punishing", target.getUniqueId() + "|" + name);

			Inventory inv = ScorchCore.getInstance().getPunishModule().getRecordGUI(target, 0);
			player.openInventory(inv);

			sp.setTempData("openInventory", "punishmentrecord");
			sp.setTempData("punishing", target.getUniqueId() + "|" + name);
		}

		return true;
	}

}
