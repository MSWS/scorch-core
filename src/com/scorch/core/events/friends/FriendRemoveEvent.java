package com.scorch.core.events.friends;

import java.util.UUID;

import org.bukkit.event.HandlerList;

import com.scorch.core.modules.players.Friendship;

public class FriendRemoveEvent extends FriendEvent {
	private static final HandlerList handlers = new HandlerList();

	private UUID remover;

	public FriendRemoveEvent(Friendship friendship, UUID remover) {
		super(friendship);
		this.remover = remover;
	}

	public UUID getRemover() {
		return remover;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
