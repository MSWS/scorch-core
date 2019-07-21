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
import com.scorch.core.modules.data.IPTracker;
import com.scorch.core.modules.data.ScorchPlayer;
import com.scorch.core.utils.Logger;
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
	private PunishModule pm;

	public PunishLoginListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		if ((pm = ScorchCore.getInstance().getPunishModule()) == null)
			return;

		Player player = event.getPlayer();
		ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(player.getUniqueId());
		Punishment active = null;

		List<Punishment> punishments = pm.getPunishments(player.getUniqueId());

		for (Punishment global : pm.getGlobalPunishments()) {
			if (global.getType() == PunishType.BLACKLIST) {
				IPTracker it = (IPTracker) ScorchCore.getInstance().getModule("IPTrackerModule");
				if (it == null) {
					Logger.error("IP Tracker Module is NULL");
					continue;
				}

				if (it.isLinked(global.getTargetUUID(), player.getUniqueId())) {
					punishments.add(global);
				}
			} else if (global.getType() == PunishType.IP_BAN) {
				if (global.getIP() == null) {
					Logger.error("Punishment " + global.getId() + " has no IP despite being an IP ban.");
					continue;
				}
				if (global.getIP().equals(event.getAddress().getHostName())
						|| global.getIP().equals(sp.getData("lastip", String.class, null))) {
					punishments.add(global);
				}
			}
		}

		punishments = punishments.stream().filter(Punishment::isActive).filter(p -> p.getType().restrictsLogin())
				.collect(Collectors.toList());

		if (punishments.isEmpty())
			return;

		
		
		Collections.sort(punishments);

		event.setResult(Result.KICK_BANNED);
		active = punishments.get(0);

		event.setKickMessage(MSG.color(active.getKickMessage()));
	}
}
