package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.scorch.core.ScorchCore;
import com.scorch.utils.MSG;
import com.scorch.utils.Utils;

public class PunishCommand implements CommandExecutor {

	public PunishCommand() {
		PluginCommand cmd = Bukkit.getPluginCommand("punish");
		cmd.setExecutor(this);
		cmd.setPermission("scorch.command.punish");
		cmd.setPermissionMessage("NOPE"); // TODO
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		Player player = (Player) sender;

		Inventory inv = Utils.getGui(player, ScorchCore.getInstance().getGui(), "punish", 0);

		player.openInventory(inv);
		return true;
	}

}
