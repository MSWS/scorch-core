package com.scorch.core.events.punishment;

import com.scorch.core.modules.punish.Punishment;

public class PunishmentCreateEvent extends PunishmentEvent{
	public PunishmentCreateEvent(Punishment p) {
		super(p);
	}
}
