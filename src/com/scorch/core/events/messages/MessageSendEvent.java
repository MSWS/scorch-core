package com.scorch.core.events.messages;

import java.util.UUID;

import org.bukkit.event.HandlerList;

import com.scorch.core.modules.communication.NetworkEvent;

public class MessageSendEvent extends NetworkEvent {
    private static final HandlerList handlers = new HandlerList();

    private UUID sender, receiver;
    private String message, senderName;

    public MessageSendEvent(UUID sender, String senderName, UUID receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.senderName = senderName;
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

    public String getSenderName() {
        return senderName;
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
