package com.scorch.core.events.punishment;

import org.bukkit.event.Event;

import com.scorch.core.modules.punish.Punishment;

public abstract class PunishmentEvent extends Event {

	protected Punishment punishment;

	public PunishmentEvent(Punishment p) {
		this.punishment = p;
	}

	public Punishment getPunishment() {
		return punishment;
	}
}
