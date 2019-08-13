package com.scorch.core.commands.staff;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

/**
 * Messaging system meant for staff only
 * 
 * <b>Permissions</b><br>
 * scorch.command.ma - Access to command<br>
 * scorch.command.ma.watch - Access to view staff PMs not sent to/from directly
 * 
 * @author imodm
 *
 */
public class MACommand extends BukkitCommand {

	public MACommand(String name) {
		super(name);
		this.setPermission("scorch.command.ma");
		this.setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length < 2) {
			MSG.tell(sender, "/ma [player] [message]");
			return true;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			MSG.tell(sender, "Unknown Player");
			return true;
		}

		if (sender instanceof Player)
			ScorchCore.getInstance().getPlayer(((Player) sender).getUniqueId()).setTempData("lastMA",
					target.getUniqueId().toString());

		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			builder.append(args[i] + " ");

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.equals(sender)) {
				MSG.tell(p,
						ScorchCore.getInstance().getMessage("maformat-sender")
								.replace("%group%", ScorchCore.getInstance().getPrefix(target))
								.replace("%player%", target.getName()).replace("%message%", builder.toString().trim()));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 1.5f);
			}
			if (p.equals(target)) {
				MSG.tell(p, ScorchCore.getInstance().getMessage("maformat-receiver")
						.replace("%group%",
								(sender instanceof Player) ? ScorchCore.getInstance().getPrefix((Player) sender) : "&4")
						.replace("%player%", sender.getName()).replace("%message%", builder.toString().trim()));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2f);
			} else if (!p.equals(sender) && p.hasPermission("scorch.command.ma.watch")) {
				MSG.tell(p, ScorchCore.getInstance().getMessage("maformat-spec")
						.replace("%senderprefix%",
								(sender instanceof Player) ? ScorchCore.getInstance().getPrefix((Player) sender) : "&4")
						.replace("%sendername%", sender.getName()).replace("%message%", builder.toString().trim())
						.replace("%receiverprefix%", ScorchCore.getInstance().getPrefix(target))
						.replace("%receivername%", target.getName()));
			}
		}

		return true;
	}

}
