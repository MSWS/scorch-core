package com.scorch.core.modules.punish;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.scorch.core.ScorchCore;
import com.scorch.core.events.punishment.PunishmentCreateEvent;
import com.scorch.core.events.punishment.PunishmentUpdateEvent;
import com.scorch.core.utils.Logger;

public class PunishmentListener implements Listener {
	private PunishModule pm = ScorchCore.getInstance().getPunishModule();

	public PunishmentListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onPunishmentCreate(PunishmentCreateEvent event) {
		pm.addExecutedPunishment(event.getPunishment());

		Logger.log("Adding punishment " + event.getPunishment().getId());
	}

	@EventHandler
	public void onPunishmentUpdate(PunishmentUpdateEvent event) {
		pm.updatePunishment(event.getPunishment());
	}
}