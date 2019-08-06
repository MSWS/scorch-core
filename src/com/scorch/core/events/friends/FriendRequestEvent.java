package com.scorch.core.events.friends;

import org.bukkit.event.HandlerList;

import com.scorch.core.modules.players.Friendship;

public class FriendRequestEvent extends FriendEvent {
	private static final HandlerList handlers = new HandlerList();

	public FriendRequestEvent(Friendship friendship) {
		super(friendship);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
