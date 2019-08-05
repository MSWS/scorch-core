package com.scorch.core.events.messages;

import java.util.UUID;

import org.bukkit.event.HandlerList;

import com.scorch.core.modules.communication.NetworkEvent;

public class MessageReceiveEvent extends NetworkEvent {
	private static final HandlerList handlers = new HandlerList();

	private UUID receiver, sender;
	private String message;

	public MessageReceiveEvent(UUID receiver, UUID sender, String message) {
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
	}

	public final UUID getSender() {
		return sender;
	}

	public final UUID getReceiver() {
		return receiver;
	}

	public final String getMessage() {
		return message;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
