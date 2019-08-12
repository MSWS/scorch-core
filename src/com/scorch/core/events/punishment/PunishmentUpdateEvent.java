package com.scorch.core.events.punishment;

import com.scorch.core.modules.punish.Punishment;

public class PunishmentUpdateEvent extends PunishmentEvent{
	public PunishmentUpdateEvent(Punishment p) {
		super(p);
	}
}
