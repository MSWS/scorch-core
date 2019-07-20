package com.scorch.core.modules.punish;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

/**
 * {@link PunishLoginListener} is the listener for when players join the server,
 * sets result to KICK_BANNED if they have an active punishment where
 * {@link PunishType#restrictsLogin()} is true
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

		List<Punishment> punishments = ScorchCore.getInstance().getPunishModule().getPunishments(player.getUniqueId());

		punishments = punishments.stream().filter(Punishment::isActive).filter(p -> p.getType().restrictsLogin())
				.collect(Collectors.toList());

		if (punishments.isEmpty())
			return;

		Collections.sort(punishments);

		event.setResult(Result.KICK_BANNED);
		Punishment active = punishments.get(0);

		event.setKickMessage(MSG.color(active.getKickMessage()));
	}
}
