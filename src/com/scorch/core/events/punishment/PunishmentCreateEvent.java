package com.scorch.core.events.punishment;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.modules.punish.Punishment;

public class PunishmentCreateEvent extends PunishmentEvent{
	public PunishmentCreateEvent(Punishment p) {
		super(p);
	}
}
