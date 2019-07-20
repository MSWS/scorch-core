package com.scorch.core.modules.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;

public class TeleportModule extends AbstractModule implements Listener {

	private Map<Player, List<Location>> history;

	public TeleportModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		history = new HashMap<Player, List<Location>>();

		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@Override
	public void disable() {
		PlayerTeleportEvent.getHandlerList().unregister(this);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();

		List<Location> ph = history.getOrDefault(player, new ArrayList<Location>());

		if (ph.contains(event.getTo())) {
			ph.remove(event.getTo());
		} else {
			ph.add(0, event.getTo());
		}
		history.put(player, ph);
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		history.remove(player);
	}

	public List<Location> getRecentTeleports(Player player) {
		return history.getOrDefault(player, new ArrayList<>());
	}

}
