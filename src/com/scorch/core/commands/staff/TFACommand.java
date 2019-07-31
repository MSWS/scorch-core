package com.scorch.core.commands.staff;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.staff.AuthenticationModule;
import com.scorch.core.utils.MSG;

public class TFACommand extends BukkitCommand {

	public TFACommand(String name) {
		super(name);
		setPermission("scorch.command.tfa");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("2fa", "2factor", "tfactor", "twofactor"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		AuthenticationModule am = ScorchCore.getInstance().getModule("AuthenticationModule",
				AuthenticationModule.class);

		if (args.length == 0) {
			MSG.tell(sender, "/" + commandLabel + " <Key>");
			return true;
		}

		if (args[0].equalsIgnoreCase("reset")) {
			OfflinePlayer target;
			if (args.length == 1) {
				if (sender instanceof Player) {
					target = (Player) sender;
				} else {
					MSG.tell(sender, "Specify player");
					return true;
				}
			} else {
				if (sender instanceof Player) {
					if (!am.authenticated(((Player) sender).getUniqueId())) {
						MSG.cTell(sender, "mustauthenticate");
						return true;
					}
				}

				target = Bukkit.getOfflinePlayer(args[1]);
			}
			ScorchPlayer sp = ScorchCore.getInstance().getPlayer(target.getUniqueId());
			sp.setData("authenticated", false);
			sp.removeData("2fakey");
			sp.removeData("lastAuthentication");

			if (target.isOnline())
				am.setupAuthentication(target.getPlayer());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player.");
			return true;
		}

		String msg = "";
		for (int i = 0; i < args.length; i++)
			msg += args[i];

		ScorchCore.getInstance().getModule("AuthenticationModule", AuthenticationModule.class)
				.authenticate((Player) sender, msg);
		return true;
	}

}
