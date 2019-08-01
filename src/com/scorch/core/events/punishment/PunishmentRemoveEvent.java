package com.scorch.core.events.punishment;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.modules.punish.Punishment;

public class PunishmentRemoveEvent extends PunishmentEvent implements Cancellable {
	@DataIgnore
	private static final HandlerList handlers = new HandlerList();

	private Punishment p;

	private boolean cancel;

	public PunishmentRemoveEvent(Punishment p) {
		super(p);
	}

	public Punishment getPunishment() {
		return p;
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
