package com.scorch.core.modules.punish;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.scorch.core.ScorchCore;

/**
 * {@link PunishLoginListener} is the listener for when players join the/a
 * server NOTE: Work in process
 * 
 * @author imodm
 *
 */
public class PunishLoginListener implements Listener {
	public PunishLoginListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();

		List<Punishment> punishments = null; // TODO

		punishments.stream().filter(Punishment::isActive).filter(p -> p.getType().restrictsLogin())
				.collect(Collectors.toList());

		if (punishments.isEmpty())
			return;

		Collections.sort(punishments, new Comparator<Punishment>() {
			@Override
			public int compare(Punishment o1, Punishment o2) {
				return o1.getDate() > o2.getDate() ? -1 : 1;
			}
		});

		event.setResult(Result.KICK_BANNED);
		Punishment active = punishments.get(0);

		event.setKickMessage(active.getReason());
	}
}
