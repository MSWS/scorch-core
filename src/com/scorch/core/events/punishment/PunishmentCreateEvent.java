package com.scorch.core.events.punishment;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.modules.punish.Punishment;

public class PunishmentCreateEvent extends PunishmentEvent implements Cancellable {
	@DataIgnore
	private static final HandlerList handlers = new HandlerList();
	private boolean cancel;

	public PunishmentCreateEvent(Punishment p) {
		super(p);
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
