package com.scorch.core.commands;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.MSG;

/**
 * Punishment History command
 * 
 * <b>Permissions</b> <br>
 * scorch.command.history - Access to command<br>
 * scorch.command.history.others - Access to view other player's history
 * 
 * @author imodm
 *
 */
public class HistoryCommand extends BukkitCommand {

	public HistoryCommand(String name) {
		super(name);
		this.setPermission("scorch.command.history");
		this.setPermissionMessage("NOPE");
		this.setAliases(Arrays.asList("ph", "punishhistory"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "you must be a player");
			return true;
		}

		Player player = (Player) sender;
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		OfflinePlayer target = null;

		if (args.length == 0) {
			if (sender instanceof Player) {
				target = player;
			} else {
				MSG.tell(sender, "/history [player]");
				return true;
			}
		} else if (sender.hasPermission("scorch.command.history.others")) {
			try {
				target = Bukkit.getOfflinePlayer(UUID.fromString(args[0]));
			} catch (IllegalArgumentException expected) {
				target = Bukkit.getOfflinePlayer(args[0]);
			}
		}

		String name = target.getName() == null ? args[0] : target.getName();

		sp.setTempData("openInventory", "viewing");
		sp.setTempData("punishing", target.getUniqueId() + "|" + name);

		player.openInventory(ScorchCore.getInstance().getPunishModule().getHistoryGUI(target, 0));

		sp.setTempData("openInventory", "viewing");
		sp.setTempData("punishing", target.getUniqueId() + "|" + name);
		return true;
	}

}
