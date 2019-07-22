package com.scorch.core.modules.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;

public class PlaytimeModule extends AbstractModule implements Listener {

	private Map<UUID, Long> loginTimes;

	private BukkitRunnable updater;

	public PlaytimeModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());

		loginTimes = new HashMap<UUID, Long>();

		updater.runTaskTimerAsynchronously(ScorchCore.getInstance(), 20 * 60, 20 * 60);
	}

	@Override
	public void disable() {
		PlayerJoinEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);

		updater.cancel();
	}

	public long getPlaytime(UUID uuid) {
		ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(uuid);
		return System.currentTimeMillis()
				- sp.getData("playtime", Long.class, loginTimes.getOrDefault(uuid, System.currentTimeMillis()));
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		loginTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(uuid);

		sp.setData("playtime", System.currentTimeMillis() - sp.getData("playtime", Long.class, loginTimes.get(uuid)));

		loginTimes.remove(uuid);
	}

}
