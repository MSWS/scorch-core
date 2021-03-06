package com.scorch.core.modules.players;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;

public class PlaytimeModule extends AbstractModule implements Listener {

	private Map<UUID, Long> loginTimes;

	public PlaytimeModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());

		loginTimes = new HashMap<UUID, Long>();

		for (Player p : Bukkit.getOnlinePlayers())
			loginTimes.put(p.getUniqueId(), System.currentTimeMillis());

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Entry<UUID, Long> entry : loginTimes.entrySet()) {
					ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(entry.getKey());

					sp.setData("playtime", sp.getData("playtime", Number.class, 0).longValue()
							+ (System.currentTimeMillis() - entry.getValue()));

					loginTimes.put(entry.getKey(), System.currentTimeMillis());
				}
			}
		}.runTaskTimerAsynchronously(ScorchCore.getInstance(), 6000, 6000); // 5 Minutes
	}

	@Override
	public void disable() {
		loginTimes.keySet().forEach(uuid -> {
			ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(uuid);

			sp.setData("playtime", sp.getData("playtime", Number.class, 0).longValue()
					+ (System.currentTimeMillis() - loginTimes.getOrDefault(uuid, System.currentTimeMillis())));
		});

		PlayerJoinEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
	}

	public long getPlaytime(UUID uuid) {
		ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(uuid);
		if (loginTimes.containsKey(uuid)) {
			return sp.getData("playtime", Number.class).longValue()
					+ (System.currentTimeMillis() - loginTimes.get(uuid));
		} else {
			return sp.getData("playtime", Number.class, 0).longValue();
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		loginTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(uuid);

		sp.setData("playtime", sp.getData("playtime", Number.class, 0).longValue()
				+ (System.currentTimeMillis() - loginTimes.getOrDefault(uuid, System.currentTimeMillis())));

		loginTimes.remove(uuid);
	}

}
