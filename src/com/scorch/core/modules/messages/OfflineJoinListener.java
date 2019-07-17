package com.scorch.core.modules.messages;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

public class OfflineJoinListener implements Listener {
	public OfflineJoinListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		OfflineMessagesModule om = (OfflineMessagesModule) ScorchCore.getInstance().getModule("OfflineMessagesModule");
		List<OfflineMessage> messages = om.getActiveMessages(event.getPlayer().getUniqueId());

		if (messages.isEmpty())
			return;

		MSG.tell(player, "&7You have " + messages.size() + " unread message" + (messages.size() == 1 ? "" : "s") + ".");

		for (OfflineMessage off : messages)
			MSG.tell(player, off.getSender() + " " + off.getMessage() + " [&e"
					+ MSG.getTime(System.currentTimeMillis() - off.getSentTime()) + "&7]");
	}
}
