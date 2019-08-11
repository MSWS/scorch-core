package com.scorch.core.modules.punish;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.scorch.core.ScorchCore;
import com.scorch.core.events.punishment.PunishmentCreateEvent;
import com.scorch.core.events.punishment.PunishmentEvent;
import com.scorch.core.events.punishment.PunishmentUpdateEvent;
import com.scorch.core.events.punishment.TestEvent;
import com.scorch.core.utils.Logger;

public class PunishmentListener implements Listener {
	private PunishModule pm = ScorchCore.getInstance().getPunishModule();

	public PunishmentListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onPunishmentCreate(PunishmentCreateEvent event) {
		Logger.log("Received new punishment: " + event.getPunishment().getReason());
		pm.addPunishment(event.getPunishment());
	}

	@EventHandler
	public void onPunishmentUpdate(PunishmentUpdateEvent event) {
		Logger.log("Received updated punishment: " + event.getPunishment().getReason());
	}

	@EventHandler
	public void onUpdate(PunishmentEvent event) {
		Logger.log("Received punishment event");
	}

	@EventHandler
	public void onTest(TestEvent event) {
		Logger.log("Received test event");
	}
}