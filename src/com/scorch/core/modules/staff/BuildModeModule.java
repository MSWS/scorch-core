package com.scorch.core.modules.staff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.players.CPlayer;
import com.scorch.core.utils.MSG;

public class BuildModeModule extends AbstractModule implements Listener {

	public BuildModeModule(String id) {
		super(id);
	}

	private Map<UUID, List<Location>> tracker;

	@Override
	public void initialize() {
		tracker = new HashMap<UUID, List<Location>>();

		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@Override
	public void disable() {
		tracker.keySet().forEach(u -> disableBuildMode(u));

		BlockPlaceEvent.getHandlerList().unregister(this);
		PlayerInteractEvent.getHandlerList().unregister(this);
		PlayerChangedWorldEvent.getHandlerList().unregister(this);
		BlockBreakEvent.getHandlerList().unregister(this);
		PlayerBucketEmptyEvent.getHandlerList().unregister(this);
		PlayerBucketFillEvent.getHandlerList().unregister(this);
		BlockPhysicsEvent.getHandlerList().unregister(this);
		BlockFadeEvent.getHandlerList().unregister(this);
		SpongeAbsorbEvent.getHandlerList().unregister(this);
	}

	public void enableBuildMode(UUID uuid) {
		if (inBuildMode(uuid))
			return;

		if (Bukkit.getPlayer(uuid) != null)
			Bukkit.getPlayer(uuid).setGameMode(GameMode.CREATIVE);

		tracker.put(uuid, new ArrayList<>());
	}

	public void disableBuildMode(UUID uuid) {
		rollback(uuid);

		if (Bukkit.getPlayer(uuid) != null)
			Bukkit.getPlayer(uuid).setGameMode(GameMode.SURVIVAL);

		tracker.remove(uuid);
	}

	public boolean inBuildMode(UUID uuid) {
		return tracker.containsKey(uuid);
	}

	public void rollback(UUID uuid) {
		tracker.getOrDefault(uuid, new ArrayList<Location>()).forEach(loc -> loc.getBlock().setType(Material.AIR));
	}

	public UUID getResponsible(Location loc) {
		for (Entry<UUID, List<Location>> entry : tracker.entrySet()) {
			if (entry.getValue().contains(loc))
				return entry.getKey();
		}
		return null;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (!tracker.containsKey(player.getUniqueId()))
			return;

		Block block = event.getBlock();

		if (block.isLiquid())
			return;

		if (event.getBlockReplacedState().getType() != Material.AIR)
			return;

		List<Material> gravity = Arrays.asList(Material.GRAVEL, Material.SAND, Material.RED_SAND, Material.ANVIL);
		if (gravity.contains(block.getType()))
			return;

		List<Location> locs = tracker.get(player.getUniqueId());
		locs.add(block.getLocation());
		tracker.put(player.getUniqueId(), locs);

		event.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (!tracker.containsKey(player.getUniqueId()))
			return;

		Block block = event.getBlock();

		List<Location> locs = tracker.get(player.getUniqueId());

		if (!locs.contains(block.getLocation()))
			return;

		event.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		if (!tracker.containsKey(player.getUniqueId()))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		if (!tracker.containsKey(player.getUniqueId()))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		if (cp.hasTempData("buildModeInspection") && event.getAction() == Action.LEFT_CLICK_BLOCK) {
			event.setCancelled(true);

			UUID uuid = getResponsible(event.getClickedBlock().getLocation());
			if (uuid == null) {
				MSG.tell(player, "No one placed that block in build mode.");
				return;
			}

			MSG.tell(player, Bukkit.getOfflinePlayer(uuid).getName() + " placed this block");

			return;
		}

		if (!tracker.containsKey(player.getUniqueId()))
			return;

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		ItemStack item = event.getItem();
		if (item.toString().toLowerCase().contains("spawn_egg"))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onChangeWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		if (!tracker.containsKey(player.getUniqueId()))
			return;
		disableBuildMode(player.getUniqueId());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockStateChange(BlockFadeEvent event) {
		for (List<Location> locs : tracker.values()) {
			if (locs.contains(event.getBlock().getLocation())) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockPhysicsEvent(BlockPhysicsEvent event) {
		for (List<Location> locs : tracker.values()) {
			if (locs.contains(event.getBlock().getLocation())) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void spongeAbsorb(SpongeAbsorbEvent event) {
		for (List<Location> locs : tracker.values()) {
			if (locs.contains(event.getBlock().getLocation())) {
				event.setCancelled(true);
				break;
			}
		}
	}
}
