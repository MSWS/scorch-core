package com.scorch.core.modules.permissions;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.Logger;

/**
 * Handles all the events that are required by {@link PermissionModule}
 *
 * @apiNote I thought it's cleaner this way
 * @author Gijs de Jong
 * @see PermissionModule
 */
public class PermissionListener implements Listener {

	private PermissionModule module;

	/**
	 * Creates a PermissionListener and also registers it
	 * 
	 * @param module the {@link PermissionModule} for easier access
	 */
	public PermissionListener(PermissionModule module) {
		this.module = module;
		// Make sure the listener is registered
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
		if (module.getPermissionPlayer(e.getUniqueId()) != null) {
			// permission player is already there, add permissions
			module.getPermissionPlayer(e.getUniqueId()).updatePermissions();
		} else {
			PermissionPlayer player = new PermissionPlayer(e.getUniqueId(), new ArrayList<>());
			module.addPlayer(e.getUniqueId(), player);
			module.getPermissionPlayer(e.getUniqueId()).updatePermissions();
		}
	}
	
	@EventHandler
	public void onPermissionUpdateEvent(PermissionUpdateEvent e) {
		Logger.info(e.getGroupToUpdate());
	}
}
