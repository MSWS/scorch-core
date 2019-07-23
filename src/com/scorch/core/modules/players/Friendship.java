package com.scorch.core.modules.players;

import java.io.Serializable;
import java.util.UUID;

public class Friendship implements Serializable {

	private UUID player, target;

	private FriendStatus status;

	public Friendship(UUID player, UUID target) {
		this.player = player;
		this.target = target;

		status = FriendStatus.REQUESTED;
	}

	public Friendship() {

	}

	public FriendStatus getStatus() {
		return status;
	}

	public UUID getPlayer() {
		return player;
	}

	public UUID getTarget() {
		return target;
	}

	enum FriendStatus {
		REQUESTED, DENIED, FRIENDS, FAVORITES;
	}
}
