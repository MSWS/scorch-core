package com.scorch.core.events.punishment;

import com.scorch.core.modules.communication.NetworkEvent;
import org.bukkit.event.Event;

import com.scorch.core.modules.punish.Punishment;

public abstract class PunishmentEvent extends NetworkEvent {

	protected Punishment punishment;

	public PunishmentEvent(Punishment p) {
		this.punishment = p;
	}

	public final Punishment getPunishment() {
		return punishment;
	}
}
