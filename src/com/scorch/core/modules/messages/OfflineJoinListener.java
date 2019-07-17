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

		MessagesModule mm = ScorchCore.getInstance().getMessages();

		MSG.tell(player, mm.getMessage("offlinemessageheader").getMessage().replace("%amo%", messages.size() + "")
				.replace("%s%", messages.size() == 1 ? "" : "s"));

		for (OfflineMessage off : messages) {
			MSG.tell(player,
					mm.getMessage("offlinemessageformat").getMessage().replace("%sender%", off.getSender())
							.replace("%message%", off.getMessage())
							.replace("%time%", MSG.getTime(System.currentTimeMillis() - off.getSentTime())));
			om.update(off, off.read());
		}
	}
}
