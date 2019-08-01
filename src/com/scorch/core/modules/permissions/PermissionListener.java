package com.scorch.core.modules.permissions;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

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
     * @param module the {@link PermissionModule} for easier access
     */
    public PermissionListener (PermissionModule module) {
        this.module = module;
        // Make sure the listener is registered
        Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin (AsyncPlayerPreLoginEvent e){
        if(module.getPermissionPlayer(e.getUniqueId()) != null){
            // permission player is already there, add permissions
            module.getPermissionPlayer(e.getUniqueId()).updatePermissions();
        }
        else {
            PermissionPlayer player = new PermissionPlayer(e.getUniqueId(), new ArrayList<>());
            module.addPlayer(e.getUniqueId(), player);
            module.getPermissionPlayer(e.getUniqueId()).updatePermissions();
        }
    }

    @EventHandler
    public void onPlayerLeave (PlayerQuitEvent e){
        onPlayerDisconnect(e.getPlayer());
    }

    @EventHandler
    public void onPlayerKick (PlayerKickEvent e){
        onPlayerDisconnect(e.getPlayer());
    }

    /**
     * Called when when the player disconnects, handled in it's own method because there's
     * {@link PlayerQuitEvent} and {@link PlayerKickEvent}
     * @param player the player that's disconnecting
     */
    private void onPlayerDisconnect (Player player) {
        module.removePlayer(player);
    }

    @EventHandler
    public void onPermissionUpdateEvent (PermissionUpdateEvent e){
        Logger.info(e.getGroupToUpdate());
    }
}
