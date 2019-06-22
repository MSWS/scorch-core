package com.scorch.core.modules.punish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;

public class PunishModule extends AbstractModule implements Listener {

	public PunishModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@Override
	public void disable() {

	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		
	}
}
