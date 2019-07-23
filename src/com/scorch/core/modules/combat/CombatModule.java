package com.scorch.core.modules.combat;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.scorch.core.modules.AbstractModule;

public class CombatModule extends AbstractModule {

	public CombatModule(String id) {
		super(id);
	}

	private boolean oldPvp = true;

	private Listener legacyListener;

	@Override
	public void initialize() {
		if (oldPvp && !Bukkit.getVersion().contains("1.8")) {
			legacyListener = new LegacyHitListener();
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1024);
				p.saveData();
			}
		}
	}

	@Override
	public void disable() {
		if (legacyListener != null)
			PlayerJoinEvent.getHandlerList().unregister(legacyListener);
	}

}
