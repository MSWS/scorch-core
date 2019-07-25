package com.scorch.core.modules.staff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.players.CPlayer;
import com.scorch.core.utils.MSG;
import com.scorch.core.utils.Utils;

public class BuildModeModule extends AbstractModule implements Listener {

	public BuildModeModule(String id) {
		super(id);
	}

	private Map<UUID, List<Location>> tracker;
	private Map<UUID, List<Entity>> entityTracker;

	private Map<UUID, ItemStack[]> invTracker;

	@Override
	public void initialize() {
		tracker = new HashMap<UUID, List<Location>>();
		entityTracker = new HashMap<UUID, List<Entity>>();
		invTracker = new HashMap<>();

		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@Override
	public void disable() {
		Iterator<UUID> it = tracker.keySet().iterator();
		while (it.hasNext())
			disableBuildMode(it.next());

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

	public boolean toggleBuildMode(UUID uuid) {
		if (inBuildMode(uuid)) {
			disableBuildMode(uuid);
		} else {
			enableBuildMode(uuid);
		}

		return inBuildMode(uuid);
	}

	public void enableBuildMode(UUID uuid) {
		if (inBuildMode(uuid))
			return;

		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			player.setGameMode(GameMode.CREATIVE);
			invTracker.put(uuid, player.getInventory().getStorageContents());
			player.getInventory().clear();
		}

		tracker.put(uuid, new ArrayList<>());
		entityTracker.put(uuid, new ArrayList<>());
	}

	public void disableBuildMode(UUID uuid) {
		rollback(uuid);

		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			player.setGameMode(GameMode.SURVIVAL);
			player.getInventory().setContents(invTracker.getOrDefault(uuid, new ItemStack[0]));
		}

		invTracker.remove(uuid);
		tracker.remove(uuid);
		entityTracker.remove(uuid);
	}

	public boolean inBuildMode(UUID uuid) {
		return tracker.containsKey(uuid);
	}

	public void rollback(UUID uuid) {
		tracker.getOrDefault(uuid, new ArrayList<Location>()).forEach(loc -> loc.getBlock().setType(Material.AIR));
		entityTracker.getOrDefault(uuid, new ArrayList<>()).forEach(ent -> ent.remove());
	}

	public UUID getResponsible(Location loc) {
		for (Entry<UUID, List<Location>> entry : tracker.entrySet()) {
			if (entry.getValue().contains(loc))
				return entry.getKey();
		}
		return null;
	}

	public UUID getResponsible(Entity ent) {
		for (Entry<UUID, List<Entity>> entry : entityTracker.entrySet()) {
			if (entry.getValue().contains(ent))
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
	public void onEntityInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if (!tracker.containsKey(player.getUniqueId()))
			return;

		ItemStack item = event.getHand() == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand()
				: player.getInventory().getItemInOffHand();

		if (item == null || item.getType() == Material.AIR)
			return;

		if (!Utils.isSpawnEgg(item.getType()))
			return;

		Entity ent = event.getRightClicked();
		EntityType type = Utils.getEntityFromSpawn(item.getType());
		if (type != ent.getType())
			return;

		if (!(ent instanceof Ageable))
			return;

		event.setCancelled(true);

		Ageable nEnt = (Ageable) player.getWorld().spawnEntity(ent.getLocation(), type);
		nEnt.setBaby();

		List<Entity> ents = entityTracker.getOrDefault(player.getUniqueId(), new ArrayList<>());
		ents.add(nEnt);
		entityTracker.put(player.getUniqueId(), ents);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		if (cp.hasTempData("buildModeInspection") && event.getAction() == Action.LEFT_CLICK_BLOCK) {
			event.setCancelled(true);

			UUID uuid = getResponsible(event.getClickedBlock().getLocation());
			if (uuid == null) {
				MSG.tell(player, ScorchCore.getInstance().getMessage("buildmodeinspectblock-natural"));
				return;
			}

			MSG.tell(player, ScorchCore.getInstance().getMessage("buildmodeinspectblock-player").replace("%player%",
					Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		}

		if (!tracker.containsKey(player.getUniqueId()))
			return;

		event.setCancelled(false);

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		ItemStack item = event.getItem();
		if (item == null || item.getType() == Material.AIR)
			return;

		EntityType type = null;

		switch (item.getType()) {
		case MINECART:
			type = EntityType.MINECART;
			break;
		case CHEST_MINECART:
			type = EntityType.MINECART_CHEST;
			break;
		case TNT_MINECART:
			type = EntityType.MINECART_TNT;
			break;
		case HOPPER_MINECART:
			type = EntityType.MINECART_HOPPER;
			break;
		case FURNACE_MINECART:
			type = EntityType.MINECART_FURNACE;
			break;
		case COMMAND_BLOCK_MINECART:
			event.setCancelled(true);
			break;
		case OAK_BOAT:
		case ACACIA_BOAT:
		case DARK_OAK_BOAT:
		case BIRCH_BOAT:
		case JUNGLE_BOAT:
		case SPRUCE_BOAT:
			type = EntityType.BOAT;
			break;
		default:
			if (Utils.isSpawnEgg(item.getType()))
				type = Utils.getEntityFromSpawn(item.getType());
			break;
		}

		if (type != null) {
			event.setCancelled(true);

			List<Entity> ents = entityTracker.getOrDefault(player.getUniqueId(), new ArrayList<>());
			ents.add(player.getWorld().spawnEntity(
					event.getClickedBlock().getRelative(event.getBlockFace()).getLocation().add(.5, 0, .5), type));
			entityTracker.put(player.getUniqueId(), ents);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void entityDamaged(EntityDamageByEntityEvent event) {
		if (event.getDamager() == null || (!(event.getDamager() instanceof Player)))
			return;

		Player damager = (Player) event.getDamager();

		CPlayer cp = ScorchCore.getInstance().getPlayer(damager);
		if (cp.hasTempData("buildModeInspection")) {
			event.setCancelled(true);

			UUID uuid = getResponsible(event.getEntity());
			if (uuid == null) {
				MSG.tell(damager, ScorchCore.getInstance().getMessage("buildmodeinspectentity-natural"));
				return;
			}

			MSG.tell(damager, ScorchCore.getInstance().getMessage("buildmodeinspectentity-player").replace("%player%",
					Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void vehicleDamageEvent(VehicleDamageEvent event) {
		if (event.getAttacker() == null || (!(event.getAttacker() instanceof Player)))
			return;

		Player damager = (Player) event.getAttacker();

		CPlayer cp = ScorchCore.getInstance().getPlayer(damager);
		if (cp.hasTempData("buildModeInspection")) {
			event.setCancelled(true);

			UUID uuid = getResponsible(event.getVehicle());
			if (uuid == null) {
				MSG.tell(damager, ScorchCore.getInstance().getMessage("buildmodeinspectentity-natural"));
				return;
			}

			MSG.tell(damager, ScorchCore.getInstance().getMessage("buildmodeinspectentity-player").replace("%player%",
					Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		}
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

	@EventHandler(priority = EventPriority.HIGH)
	public void structureGrow(StructureGrowEvent event) {
		Player player = event.getPlayer();
		if (!tracker.containsKey(player.getUniqueId()))
			return;

		List<Location> locs = tracker.get(player.getUniqueId());

		if (!locs.contains(event.getLocation()))
			return;

		locs.addAll(event.getBlocks().stream()
				.filter(b -> !b.getBlock().getRelative(BlockFace.UP).getType().toString().contains("SAPLING"))
				.map(b -> b.getLocation()).collect(Collectors.toList()));
	}

	@EventHandler
	public void blockSpread(BlockSpreadEvent event) {
		for (List<Location> locs : tracker.values()) {
			if (locs.contains(event.getSource().getLocation()) || locs.contains(event.getBlock().getLocation())) {
				event.setCancelled(true);
				break;
			}
		}
	}
}
