package com.scorch.core.modules.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.scorch.core.ScorchCore;

public class ChatListener implements Listener {
	public ChatListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@SuppressWarnings("unused")
	@EventHandler // TODO
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		event.setFormat("");
	}
}
