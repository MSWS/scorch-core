package com.scorch.core.modules.chat;

import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.scorch.core.modules.AbstractModule;

public class ChatModule extends AbstractModule {

	public ChatModule(String id) {
		super(id);
	}

	private Listener chatListener;

	@Override
	public void initialize() {
		chatListener = new ChatListener();
	}

	@Override
	public void disable() {
		AsyncPlayerChatEvent.getHandlerList().unregister(chatListener);
	}
}
