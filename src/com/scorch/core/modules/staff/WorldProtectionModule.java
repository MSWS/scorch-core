package com.scorch.core.modules.staff;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;

/**
 * General world protection, by default most actions are cancelled, gamemodes,
 * minigames, etc. are expected to listent to {@link EventPriority.HIGH} to
 * override this
 * 
 * @see BuildModeModule
 * 
 * @author imodm
 *
 */
public class WorldProtectionModule extends AbstractModule implements Listener {

	public WorldProtectionModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@Override
	public void disable() {
		BlockPlaceEvent.getHandlerList().unregister(this);
		BlockBreakEvent.getHandlerList().unregister(this);
		PlayerBucketEmptyEvent.getHandlerList().unregister(this);
		PlayerBucketFillEvent.getHandlerList().unregister(this);
		PlayerInteractEvent.getHandlerList().unregister(this);
		PlayerDropItemEvent.getHandlerList().unregister(this);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBucketFillEvent(PlayerBucketFillEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

}
