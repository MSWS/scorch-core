package com.scorch.core.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.FriendModule;
import com.scorch.core.modules.players.Friendship;
import com.scorch.core.modules.players.Friendship.FriendStatus;
import com.scorch.core.utils.MSG;

public class FriendCommand extends BukkitCommand {

	private FriendModule friends;

	public FriendCommand(String name) {
		super(name);
		setPermission("scorch.command.friend");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("f"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

		if (friends == null)
			friends = ScorchCore.getInstance().getModule("FriendModule", FriendModule.class);

		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, "Sorry, only players can have friends.");
			return true;
		}

		Player player = (Player) sender;
		OfflinePlayer target;

		List<Friendship> sentFriends = friends.getFriends(player.getUniqueId()).stream()
				.filter(f -> f.getPlayer().equals(player.getUniqueId())).collect(Collectors.toList()),
				receivedFriends = friends.getFriends(player.getUniqueId()).stream()
						.filter(f -> f.getTarget().equals(player.getUniqueId())).collect(Collectors.toList());

		List<Friendship> sentRequests = sentFriends.stream().filter(f -> f.getStatus() == FriendStatus.REQUESTED)
				.collect(Collectors.toList()),
				receivedRequests = receivedFriends.stream().filter(f -> f.getStatus() == FriendStatus.REQUESTED)
						.collect(Collectors.toList());

		if (args.length == 0) {
			if (!sentRequests.isEmpty()) {
				MSG.tell(sender, "You have sent " + sentRequests.size() + " pending friend request"
						+ (sentRequests.size() == 1 ? "" : "s") + ".");

				for (Friendship f : sentRequests) {
					MSG.tell(sender, "Friend request to " + Bukkit.getOfflinePlayer(f.getTarget()).getName());
				}
			}

			if (!receivedRequests.isEmpty()) {
				MSG.tell(sender, "You have " + receivedRequests.size() + " pending friend request for you"
						+ (receivedRequests.size() == 1 ? "" : "s") + ".");

				for (Friendship f : receivedRequests) {
					MSG.tell(sender, "Friend request from " + Bukkit.getOfflinePlayer(f.getTarget()).getName());
				}
			}

			for (Friendship f : friends.getFriends(player.getUniqueId()).stream()
					.filter(f -> f.getStatus() != FriendStatus.REQUESTED).collect(Collectors.toList())) {
				String name = Bukkit.getOfflinePlayer(f.getTarget()).getName();
				if (f.getTarget().equals(player.getUniqueId())) {
					MSG.tell(sender, Bukkit.getOfflinePlayer(f.getPlayer()).getName() + " [REQUEST]: " + f.getStatus());
				} else {
					MSG.tell(sender, name + ": " + f.getStatus());
				}
			}
			return true;
		}

		switch (args[0].toLowerCase()) {
		case "list":
			for (Friendship f : friends.getFriends(player.getUniqueId())) {
				MSG.tell(sender, f.getTarget() + ": " + f.getStatus());
			}
			break;
		case "remove":
			target = Bukkit.getOfflinePlayer(args[1]);
			Friendship active = friends.getFriends(player.getUniqueId()).stream()
					.filter(f -> f.getTarget().equals(target.getUniqueId())).findFirst().orElse(null);

			if (active == null) {
				MSG.tell(sender,
						"You are not friends with or have a request to be friends with " + target.getName() + ".");
				return true;
			}

			friends.deleteFriendship(active);
			MSG.tell(sender, "Successfully removed " + target.getName() + " from your friends.");
			break;
		case "favorite":
			break;
		default:
			target = Bukkit.getOfflinePlayer(args[0]);

			Friendship pending = friends.getFriends(player.getUniqueId()).stream()
					.filter(f -> f.getStatus() == FriendStatus.REQUESTED)
					.filter(f -> f.getPlayer().equals(target.getUniqueId()))
					.filter(f -> f.getTarget().equals(player.getUniqueId())).findFirst().orElse(null);

			if (pending != null) {
				pending.setStatus(FriendStatus.FRIENDS);
				MSG.tell(sender, "You and " + target.getName() + " are now friends!");
				friends.updateFriendship(player.getUniqueId(), target.getUniqueId(), FriendStatus.FRIENDS);
				return true;
			}

			if (friends.areFriends(player.getUniqueId(), target.getUniqueId())) {
				MSG.tell(sender, "You and " + target.getName() + " are already friends.");
				return true;
			}

			pending = friends.getFriends(player.getUniqueId()).stream()
					.filter(f -> f.getStatus() == FriendStatus.REQUESTED)
					.filter(f -> f.getTarget().equals(target.getUniqueId())).findFirst().orElse(null);

			if (pending != null) {
				MSG.tell(sender, "You have already sent a friend request to " + target.getName());
				return true;
			}

			Friendship friendship = new Friendship(player.getUniqueId(), target.getUniqueId());

			MSG.tell(sender, "Successfully sent a friend request to " + target.getName());

			friends.addFriendship(friendship);
			break;
		}

		return true;
	}

}
