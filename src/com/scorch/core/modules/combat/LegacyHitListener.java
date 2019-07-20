package com.scorch.core.modules.combat;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.scorch.core.ScorchCore;

public class LegacyHitListener implements Listener {
	public LegacyHitListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1024);
		player.saveData();
	}
}
