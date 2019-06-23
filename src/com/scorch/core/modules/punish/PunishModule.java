package com.scorch.core.modules.punish;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.scorch.core.commands.PunishCommand;
import com.scorch.core.modules.AbstractModule;

public class PunishModule extends AbstractModule {

	public PunishModule(String id) {
		super(id);
	}

	private Listener joinListener;

	@Override
	public void initialize() {
		new PunishCommand();
		joinListener = new PunishLoginListener();
	}

	@Override
	public void disable() {
		PlayerLoginEvent.getHandlerList().unregister(joinListener);
	}

	public void addPunishment(Punishment punishment) {
		punishment.execute();
	}

}
