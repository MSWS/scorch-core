package com.scorch.core.modules.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;

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
					});
				} catch (DataObtainException | NoDefaultConstructorException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
	}

	public List<Friendship> getFriends(UUID player) {
		return friendships.getOrDefault(player, new ArrayList<>());
	}

	public void addFriendship(Friendship friend) {
		List<Friendship> tf = friendships.getOrDefault(friend.getPlayer(), new ArrayList<Friendship>());
		tf.add(friend);
		friendships.put(friend.getPlayer(), tf);

		ScorchCore.getInstance().getDataManager().saveObjectAsync("friends", friend);
	}

}
