package com.scorch.core.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.CPlayer;
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
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		OfflinePlayer target = null;

		if (args.length == 0) {
			if (sender instanceof Player) {
				target = player;
			} else {
				MSG.tell(sender, "/history [player]");
				return true;
			}
		} else if (sender.hasPermission("scorch.command.history.others")) {
			target = Bukkit.getOfflinePlayer(args[0]);
		}

		cp.setTempData("openInventory", "viewing");
		cp.setTempData("punishing", target.getUniqueId() + "|" + target.getName());

		player.openInventory(ScorchCore.getInstance().getPunishModule().getHistoryGUI(target, 0));

		cp.setTempData("openInventory", "viewing");
		cp.setTempData("punishing", target.getUniqueId() + "|" + target.getName());
		return true;
	}

}
