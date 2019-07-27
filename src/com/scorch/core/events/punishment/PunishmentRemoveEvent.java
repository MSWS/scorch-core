package com.scorch.core.events.punishment;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.scorch.core.modules.punish.Punishment;

public class PunishmentRemoveEvent extends PunishmentEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancel;

	public PunishmentRemoveEvent(Punishment p) {
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
