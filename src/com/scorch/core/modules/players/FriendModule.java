package com.scorch.core.modules.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.scorch.core.modules.data.exceptions.DataPrimaryKeyException;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.modules.messages.OfflineMessage;
import com.scorch.core.modules.messages.OfflineMessagesModule;
import com.scorch.core.modules.players.Friendship.FriendStatus;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.MSG;

public class FriendModule extends AbstractModule {

	public FriendModule(String id) {
		super(id);
	}

	private Map<UUID, List<Friendship>> friendships;

	@Override
	public void initialize() {
		reloadFriendships();
	}

	@Override
	public void disable() {
		friendships = new HashMap<>();
	}

	public void reloadFriendships() {
		friendships = new HashMap<UUID, List<Friendship>>();
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					ScorchCore.getInstance().getDataManager().createTable("friends", Friendship.class);
					ScorchCore.getInstance().getDataManager().getAllObjects("friends").forEach(f -> {
						Friendship friend = (Friendship) f;
						List<Friendship> tf = friendships.getOrDefault(friend.getPlayer(), new ArrayList<Friendship>());
						tf.add(friend);
						friendships.put(friend.getPlayer(), tf);

						tf = friendships.getOrDefault(friend.getTarget(), new ArrayList<Friendship>());
						tf.add(friend);
						friendships.put(friend.getTarget(), tf);
					});

					Logger.log("&aSuccessfully loaded &e" + friendships.size() + " &afriendship"
							+ (friendships.size() == 1 ? "" : "s") + ".");
				} catch (DataObtainException | NoDefaultConstructorException | DataPrimaryKeyException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
	}

	public List<Friendship> getFriends(UUID player) {
		return friendships.getOrDefault(player, new ArrayList<>());
	}

	public void updateFriendship(UUID player, UUID target, FriendStatus status) {
		Friendship newF = new Friendship(player, target);
		newF.setStatus(status);

		ScorchCore.getInstance().getDataManager().updateObjectAsync("friends", newF);
	}

	public void addFriendship(Friendship friend) {
		List<Friendship> tf = friendships.getOrDefault(friend.getPlayer(), new ArrayList<Friendship>());
		tf.add(friend);
		friendships.put(friend.getPlayer(), tf);

		ScorchCore.getInstance().getDataManager().saveObjectAsync("friends", friend);
	}

	public void deleteFriendship(Friendship friend) {
		List<Friendship> tf = friendships.getOrDefault(friend.getPlayer(), new ArrayList<Friendship>());
		tf.remove(friend);
		tf = friendships.getOrDefault(friend.getTarget(), new ArrayList<Friendship>());
		tf.remove(friend);

		UUID player = friend.getPlayer(), target = friend.getTarget();

		if (Bukkit.getOfflinePlayer(target).isOnline()) {
			MSG.tell(Bukkit.getPlayer(target), Bukkit.getOfflinePlayer(player).getName() + " unfriended you.");
		} else {
			OfflineMessagesModule omm = ScorchCore.getInstance().getModule("OfflineMessagesModule",
					OfflineMessagesModule.class);
			omm.addMessage(new OfflineMessage("Friends", target,
					Bukkit.getOfflinePlayer(player).getName() + " unfriended you."));
		}

		ScorchCore.getInstance().getDataManager().deleteObjectAsync("friends",
				new SQLSelector("player", friend.getPlayer().toString()),
				new SQLSelector("target", friend.getTarget().toString()));
	}

	public boolean areFriends(UUID player, UUID target) {
		List<Friendship> friends = getFriends(player);
		for (Friendship f : friends) {
			if (f.getTarget().equals(target)
					&& (f.getStatus() == FriendStatus.FRIENDS || f.getStatus() == FriendStatus.FAVORITES))
				return true;
		}
		return false;
	}

}
