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
import com.scorch.core.modules.staff.TrustModule;
import com.scorch.core.modules.staff.TrustModule.PublicTrust;
import com.scorch.core.utils.MSG;

/**
 * Punish command meant for staff
 * 
 * <b>Permissions</b><br>
 * scorch.command.punish - Access to command<br>
 * (Modify GUI to define permissions for each item/punishment)
 * 
 * @author imodm
 *
 */
public class PunishCommand extends BukkitCommand {

	public PunishCommand(String name) {
		super(name);
		this.setPermission("scorch.command.punish");
		this.setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		this.setAliases(Arrays.asList("p"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		Player player = (Player) sender;
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		if (args.length < 2) {
			MSG.tell(sender, "/punish [player] [reason]");
			return true;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < args.length; i++)
			sb.append(args[i] + " ");

		String reason = sb.toString().trim();

		OfflinePlayer target;

		try {
			target = Bukkit.getOfflinePlayer(UUID.fromString(args[0]));
		} catch (IllegalArgumentException expected) {
			target = Bukkit.getOfflinePlayer(args[0]);
		}

		String name = target.getName() == null ? args[0] : target.getName();

		TrustModule tm = ScorchCore.getInstance().getModule("TrustModule", TrustModule.class);

		sp.setTempData("openInventory", "punish");
		sp.setTempData("punishing", target.getUniqueId() + "|" + name);
		sp.setTempData("reason", reason);
		sp.setTempData("trustenum", MSG.color(PublicTrust.get(tm.getTrust(target.getUniqueId())).getColored()));

		player.openInventory(ScorchCore.getInstance().getPunishModule().getPunishGUI(player, target));

		sp.setTempData("openInventory", "punish");
		sp.setTempData("punishing", target.getUniqueId() + "|" + name);
		sp.setTempData("reason", reason);
		sp.setTempData("trustenum", MSG.color(PublicTrust.get(tm.getTrust(target.getUniqueId())).getColored()));
		return true;
	}

}
