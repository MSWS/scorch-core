package com.scorch.core.modules.players;

import java.io.Serializable;
import java.util.UUID;

import com.scorch.core.ScorchCore;
import com.scorch.core.events.friends.FriendRequestEvent;
import com.scorch.core.modules.communication.exceptions.WebSocketException;

public class Friendship implements Serializable {

	private UUID player, target;

	private FriendStatus status;

	public Friendship(UUID player, UUID target) {
		this.player = player;
		this.target = target;

		status = FriendStatus.REQUESTED;

		FriendRequestEvent fre = new FriendRequestEvent(this);
		try {
			ScorchCore.getInstance().getCommunicationModule().dispatchEvent(fre);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
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

	public void setStatus(FriendStatus status) {
		this.status = status;
	}

	public enum FriendStatus {
		REQUESTED, DENIED, FRIENDS;
	}
}
