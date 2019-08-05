package com.scorch.core.events.friends;

import org.bukkit.event.HandlerList;

import com.scorch.core.modules.communication.NetworkEvent;
import com.scorch.core.modules.players.Friendship;

public class FriendEvent extends NetworkEvent {
	private static final HandlerList handlers = new HandlerList();

	protected Friendship friend;

	public FriendEvent(Friendship friendship) {
		this.friend = friendship;
	}

	public final Friendship getFriendship() {
		return friend;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
