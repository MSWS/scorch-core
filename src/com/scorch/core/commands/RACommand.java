package com.scorch.core.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.MSG;

/**
 * Extension of /MA command, used to send a staff message to the most recent
 * player you sent a MA to
 * 
 * <b>Permissions</b><br>
 * scorch.command.ra - Access to command<br>
 * scorch.command.ma.watch - Access to view staff PMs not sent to/from directly
 * 
 * @author imodm
 *
 */
public class RACommand extends BukkitCommand {

	public RACommand(String name) {
		super(name);
		this.setPermission("scorch.command.ra");
		this.setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

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

		if (args.length < 1) {
			MSG.tell(sender, "/ra [message]");
			return true;
		}

		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(((Player) sender).getUniqueId());

		Player target = null;
		if (sender instanceof Player) {
			if (!sp.hasTempData("lastMA")) {
				MSG.tell(sender, "You have not MA'd anyone");
				return true;
			}

			target = Bukkit.getPlayer(UUID.fromString(ScorchCore.getInstance()
					.getPlayer(((Player) sender).getUniqueId()).getTempData("lastMA", String.class)));
		} else {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++)
			builder.append(args[i] + " ");

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.equals(sender)) {
				MSG.tell(p, ScorchCore.getInstance().getMessage("maformat-sender").replace("%group%", "")
						.replace("%player%", target.getName()).replace("%message%", builder.toString().trim()));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 1.5f);
			}
			if (p.equals(target)) {
				MSG.tell(p, ScorchCore.getInstance().getMessage("maformat-receiver").replace("%group%", "")
						.replace("%player%", sender.getName()).replace("%message%", builder.toString().trim()));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2f);
			} else if (!p.equals(sender) && p.hasPermission("scorch.command.ma.watch")) {
				MSG.tell(p, ScorchCore.getInstance().getMessage("maformat-spec").replace("%senderprefix%", "")
						.replace("%sendername%", sender.getName()).replace("%message%", builder.toString().trim())
						.replace("%receiverprefix%", "").replace("%receivername%", target.getName()));
			}
		}

		return true;
	}

}
