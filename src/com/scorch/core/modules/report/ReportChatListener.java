package com.scorch.core.modules.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.scorch.core.ScorchCore;

public class ReportChatListener implements Listener {
	private Map<UUID, List<String>> history;

	private final int lines = 100;

	private final SimpleDateFormat sdf = new SimpleDateFormat("h:mm");

	public ReportChatListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());

		history = new HashMap<UUID, List<String>>();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(AsyncPlayerChatEvent event) {
		String line = "[" + sdf.format(System.currentTimeMillis()) + "] " + event.getPlayer().getName() + ": "
				+ event.getMessage();
		for (Player p : event.getRecipients()) {
			List<String> logs = getLogs(p.getUniqueId());
			logs.add(line);
			if (logs.size() >= lines)
				logs = logs.subList(logs.size() - lines, logs.size());
			history.put(p.getUniqueId(), logs);
		}
	}

	public List<String> getLogs(UUID uuid) {
		return history.getOrDefault(uuid, new ArrayList<String>());
	}

}
