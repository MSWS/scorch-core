package com.scorch.core.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.CPlayer;
import com.scorch.core.utils.MSG;

public class RACommand extends BukkitCommand {

	public RACommand(String name) {
		super(name);
		this.setPermission("scorch.command.ra");
		this.setPermissionMessage(ScorchCore.getInstance().getMessages().getMessage("noperm").getMessage());
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			MSG.tell(sender, "/ra [message]");
			return true;
		}

		CPlayer cp = ScorchCore.getInstance().getPlayer((Player) sender);

		Player target = null;
		if (sender instanceof Player) {
			if (!cp.hasTempData("lastMA")) {
				MSG.tell(sender, "You have not MA'd anyone");
				return true;
			}

			target = Bukkit.getPlayer(
					UUID.fromString(ScorchCore.getInstance().getPlayer((Player) sender).getTempString("lastMA")));
		} else {
			MSG.tell(sender, "You must be a player");
			return true;
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++)
			builder.append(args[i] + " ");

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.equals(sender)) {
				MSG.tell(p, "&5-> &6" + target.getName() + " &d" + builder.toString().trim());
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 1.5f);
			}
			if (p.equals(target)) {
				MSG.tell(p, "&5<- &6" + sender.getName() + " &d" + builder.toString().trim());
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2f);
			} else if (!p.equals(sender) && p.hasPermission("scorch.command.ma.watch")) {
				MSG.tell(p,
						"&6" + sender.getName() + " &5-> &6" + target.getName() + " &d" + builder.toString().trim());
			}
		}

		return true;
	}

}
