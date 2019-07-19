package com.scorch.core.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.CPlayer;
import com.scorch.core.utils.MSG;

public class PunishCommand extends BukkitCommand {

	public PunishCommand(String name) {
		super(name);
		this.setPermission("scorch.command.punish");
		this.setPermissionMessage(ScorchCore.getInstance().getMessages().getMessage("noperm").toString());
		this.setAliases(Arrays.asList("p"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		Player player = (Player) sender;

		if (args.length < 2) {
			MSG.tell(sender, "/punish [player] [reason]");
			return true;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < args.length; i++)
			sb.append(args[i] + " ");

		String reason = sb.toString().trim();

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);

		cp.setTempData("openInventory", "punish");
		cp.setTempData("punishing", target.getUniqueId() + "|" + args[0]);
		cp.setTempData("reason", reason);

		player.openInventory(ScorchCore.getInstance().getPunishModule().getPunishGUI(player, target));

		cp.setTempData("openInventory", "punish");
		cp.setTempData("punishing", target.getUniqueId() + "|" + args[0]);
		cp.setTempData("reason", reason);
		return true;
	}

}
