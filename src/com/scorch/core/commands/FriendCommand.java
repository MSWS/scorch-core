package com.scorch.core.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.FriendModule;
import com.scorch.core.modules.players.Friendship;
import com.scorch.core.utils.MSG;

public class FriendCommand extends BukkitCommand {

	public FriendCommand(String name) {
		super(name);
		setPermission("scorch.command.friend");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("f"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "Sorry, only players can have friends.");
			return true;
		}

		if (args.length == 0) {
			// send friend help message
			return true;
		}

		Player player = (Player) sender;
		ScorchCore.getInstance().getDataManager().getScorchPlayer(player.getUniqueId());

		switch (args[0].toLowerCase()) {
		case "list":
			for (Friendship f : ScorchCore.getInstance().getModule("FriendModule", FriendModule.class)
					.getFriends(player.getUniqueId())) {
				MSG.tell(sender, f.getTarget() + ": " + f.getStatus());
			}

			break;
		case "remove":
			break;
		case "favorite":
			break;
		default:
			OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
			Friendship friendship = new Friendship(player.getUniqueId(), target.getUniqueId());

			MSG.tell(sender, "Successfully sent a friend request to " + target.getName());
			ScorchCore.getInstance().getModule("FriendModule", FriendModule.class).addFriendship(friendship);
			break;
		}

		return true;
	}

}
