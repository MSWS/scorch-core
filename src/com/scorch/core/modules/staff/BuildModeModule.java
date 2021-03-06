package com.scorch.core.modules.staff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.utils.MSG;
import com.scorch.core.utils.Utils;

/**
 * This module manages and listens to all events related to players in build
 * mode
 * 
 * <b>Permissions</b><br>
 * scorch.buildmode.modifyothers - Access to modify other staff's buildmode
 * while in buildmode
 * 
 * @author imodm
 *
 */
public class BuildModeModule extends AbstractModule implements Listener {

	public BuildModeModule(String id) {
		super(id);
	}

	private Map<UUID, List<Location>> tracker;
	private Map<UUID, List<Entity>> entityTracker;

	private Map<UUID, ItemStack[]> invTracker;
	private Map<UUID, BuildStatus> status;

	private boolean changeWorld, changeQuit, changeGamemode;

	@Override
	public void initialize() {
		tracker = new HashMap<UUID, List<Location>>();
		entityTracker = new HashMap<UUID, List<Entity>>();
		invTracker = new HashMap<>();
		status = new HashMap<>();

		changeWorld = ScorchCore.getInstance().getConfig().getBoolean("Rollback.OnChangeWorld");
		changeQuit = ScorchCore.getInstance().getConfig().getBoolean("Rollback.OnQuit");
		changeGamemode = ScorchCore.getInstance().getConfig().getBoolean("Rollback.OnGameModechange");

		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@Override
	public void disable() {
		List<UUID> uuids = new ArrayList<>(tracker.keySet());
		for (int i = 0; i < uuids.size(); i++) {
			setStatus(uuids.get(i), BuildStatus.NONE);
			rollback(uuids.get(i), true);
		}

		BlockPlaceEvent.getHandlerList().unregister(this);
		BlockBreakEvent.getHandlerList().unregister(this);
		PlayerInteractEntityEvent.getHandlerList().unregister(this);
		PlayerInteractEvent.getHandlerList().unregister(this);
		PlayerChangedWorldEvent.getHandlerList().unregister(this);
		BlockBreakEvent.getHandlerList().unregister(this);
		PlayerBucketEmptyEvent.getHandlerList().unregister(this);
		PlayerBucketFillEvent.getHandlerList().unregister(this);
		BlockPhysicsEvent.getHandlerList().unregister(this);
		BlockFadeEvent.getHandlerList().unregister(this);
		SpongeAbsorbEvent.getHandlerList().unregister(this);
		StructureGrowEvent.getHandlerList().unregister(this);
		BlockSpreadEvent.getHandlerList().unregister(this);
		EntityExplodeEvent.getHandlerList().unregister(this);
		CreatureSpawnEvent.getHandlerList().unregister(this);
		BlockFertilizeEvent.getHandlerList().unregister(this);
		ExplosionPrimeEvent.getHandlerList().unregister(this);
		PlayerDropItemEvent.getHandlerList().unregister(this);
	}

	public boolean inBuildMode(UUID uuid) {
		return getStatus(uuid) == BuildStatus.BUILD;
	}

	public void rollback(UUID uuid, boolean quick) {
		entityTracker.getOrDefault(uuid, new ArrayList<>()).forEach(ent -> ent.remove());

		if (quick) {
			tracker.getOrDefault(uuid, new ArrayList<Location>()).forEach(loc -> loc.getBlock().setType(Material.AIR));

			tracker.put(uuid, new ArrayList<>());
			return;
		} else {
			int rollbackBlocks = Math.max(tracker.getOrDefault(uuid, new ArrayList<>()).size() / 500, 1);
			long start = System.currentTimeMillis();

			BukkitRunnable runnable = new BukkitRunnable() {
				int pos = 0;
				List<Location> loc = tracker.getOrDefault(uuid, new ArrayList<>());

				@Override
				public void run() {
					int off = rollbackBlocks;
					if (pos >= loc.size()) {
						tracker.put(uuid, new ArrayList<>());
						cancel();
						return;
					}
					if (System.currentTimeMillis() - start > 10000) {
						off += ((System.currentTimeMillis() - start) / 10000);
					}
					for (int i = 0; i < off && pos < loc.size(); i++) {
						Location l = loc.get(pos);
						if (l.getBlock().getType() == Material.AIR) {
							pos++;
							continue;
						}
						l.getWorld().playSound(l, Utils.getBreakSound(l.getBlock().getType()).bukkitSound(), 1, 1);
						l.getBlock().setType(Material.AIR);
						pos++;
					}
				}
			};

			runnable.runTaskTimer(ScorchCore.getInstance(), 0, 1);
		}

		invTracker.remove(uuid);
		entityTracker.remove(uuid);
	}

	public boolean rollback(UUID uuid, int blocks) {
		if (blocks > tracker.getOrDefault(uuid, new ArrayList<>()).size())
			return false;

		List<Location> b = tracker.getOrDefault(uuid, new ArrayList<>()).subList(
				tracker.getOrDefault(uuid, new ArrayList<>()).size() - blocks,
				tracker.getOrDefault(uuid, new ArrayList<>()).size());

		int rollbackBlocks = Math.max(tracker.getOrDefault(uuid, new ArrayList<>()).size() / 500, 1);
		long start = System.currentTimeMillis();

		BukkitRunnable runnable = new BukkitRunnable() {
			int pos = b.size() - 1;

			@Override
			public void run() {
				if (pos < 0) {
					tracker.getOrDefault(uuid, new ArrayList<>()).removeAll(b);
					cancel();
					return;
				}
				int off = rollbackBlocks;
				if (System.currentTimeMillis() - start > 10000) {
					off += ((System.currentTimeMillis() - start) / 10000);
				}
				for (int i = 0; i < off && pos >= 0; i++) {
					Location l = b.get(pos);
					if (l.getBlock().getType() == Material.AIR) {
						pos--;
						continue;
					}
					l.getWorld().playSound(l, Utils.getBreakSound(l.getBlock().getType()).bukkitSound(), 2, 1);
					l.getBlock().setType(Material.AIR);
					pos--;
				}
			}
		};

		runnable.runTaskTimer(ScorchCore.getInstance(), 0, 1);
		return true;
	}

	public Set<UUID> getBuilders() {
		return tracker.keySet();
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
		if (event instanceof BlockMultiPlaceEvent)
			return;

		Player player = event.getPlayer();

		if (getStatus(player.getUniqueId()) == BuildStatus.OVERRIDE) {
			event.setCancelled(false);
			return;
		}

		if (getStatus(player.getUniqueId()) != BuildStatus.BUILD)
			return;

		Block block = event.getBlock();

		if (block.isLiquid())
			return;

		if (event.getBlockReplacedState().getType() != Material.AIR)
			return;

		List<Location> locs = tracker.get(player.getUniqueId());
		locs.add(block.getLocation());
		tracker.put(player.getUniqueId(), locs);

		event.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockMultiPlaceEvent event) {
		Player player = event.getPlayer();

		if (getStatus(player.getUniqueId()) == BuildStatus.OVERRIDE) {
			event.setCancelled(false);
			return;
		}

		if (getStatus(player.getUniqueId()) != BuildStatus.BUILD)
			return;

		Block block = event.getBlock();

		if (block.isLiquid())
			return;

		if (event.getReplacedBlockStates().stream().anyMatch(bs -> bs.getType() != Material.AIR))
			return;

		List<Material> gravity = Arrays.asList(Material.GRAVEL, Material.SAND, Material.RED_SAND, Material.ANVIL);
		if (gravity.contains(block.getType()))
			return;

		List<Location> locs = tracker.get(player.getUniqueId());
		locs.addAll(event.getReplacedBlockStates().stream().map(bs -> bs.getLocation()).collect(Collectors.toList()));
		tracker.put(player.getUniqueId(), locs);

		event.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();

		if (getStatus(player.getUniqueId()) == BuildStatus.OVERRIDE) {
			event.setCancelled(false);
			return;
		}

		if (getStatus(player.getUniqueId()) != BuildStatus.BUILD)
			return;

		Block block = event.getBlock();

		List<Location> locs = tracker.get(player.getUniqueId());

		if (!(player.hasPermission("scorch.buildmode.modifyothers") && isProtected(block))
				&& !locs.contains(block.getLocation())) {
			return;
		}

		event.setCancelled(false);
		event.setDropItems(false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();

		if (getStatus(player.getUniqueId()) == BuildStatus.OVERRIDE) {
			event.setCancelled(false);
			return;
		}

		if (getStatus(player.getUniqueId()) != BuildStatus.BUILD)
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

		if (getStatus(player.getUniqueId()) == BuildStatus.OVERRIDE) {
			event.setCancelled(false);
			return;
		}

		if (getStatus(player.getUniqueId()) == BuildStatus.INSPECT && event.getAction() == Action.LEFT_CLICK_BLOCK) {
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

		if (getStatus(player.getUniqueId()) != BuildStatus.BUILD)
			return;

		event.setCancelled(false);

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		ItemStack item = event.getItem();
		if (item == null || item.getType() == Material.AIR)
			return;

		if (item.getType() == Material.FLINT_AND_STEEL) {
			for (BlockFace face : BlockFace.values()) {
				if (event.getClickedBlock().getRelative(event.getBlockFace()).getRelative(face)
						.getType() == Material.TNT) {
					event.setCancelled(true);
					break;
				}
			}
			return;
		}

		if (item.getType() == Material.TNT) {
			for (BlockFace face : BlockFace.values()) {
				Block block = event.getClickedBlock().getRelative(event.getBlockFace()).getRelative(face);
				if (block.isBlockPowered() || isRedstone(block.getType())) {
					event.setCancelled(true);
					break;
				}
			}
			if (isRedstone(event.getClickedBlock().getType()) || event.getClickedBlock().isBlockPowered())
				event.setCancelled(true);
			return;
		}

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
		case END_CRYSTAL:
			type = EntityType.ENDER_CRYSTAL;
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

			if (player.getGameMode() != GameMode.CREATIVE) {
				item.setAmount(item.getAmount() - 1);
				if (item.getAmount() == 0)
					item.setType(Material.AIR);

				if (event.getHand() == EquipmentSlot.HAND) {
					player.getInventory().setItemInMainHand(item);
				} else {
					player.getInventory().setItemInOffHand(item);
				}
			}

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

		if (getStatus(damager.getUniqueId()) == BuildStatus.OVERRIDE) {
			event.setCancelled(false);
			return;
		}

		if (getStatus(damager.getUniqueId()) == BuildStatus.INSPECT) {
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

		if (getStatus(damager.getUniqueId()) == BuildStatus.OVERRIDE) {
			event.setCancelled(false);
			return;
		}

		if (getStatus(damager.getUniqueId()) == BuildStatus.INSPECT) {
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
		if (!changeWorld)
			return;
		if (getStatus(player.getUniqueId()) != BuildStatus.BUILD)
			return;

		setStatus(player.getUniqueId(), BuildStatus.NONE);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (!changeQuit)
			return;
		if (getStatus(player.getUniqueId()) != BuildStatus.BUILD)
			return;

		setStatus(player.getUniqueId(), BuildStatus.NONE);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onGamemodeChange(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();
		if (!changeGamemode)
			return;
		if (getStatus(player.getUniqueId()) != BuildStatus.BUILD)
			return;
		if (event.getNewGameMode() == GameMode.CREATIVE)
			return;

		setStatus(player.getUniqueId(), BuildStatus.NONE);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockStateChange(BlockFadeEvent event) {
		if (!isProtected(event.getBlock()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void portalCreate(PortalCreateEvent event) {
		if (!event.getBlocks().stream().anyMatch(bs -> isProtected(bs.getLocation())))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockPhysicsEvent(BlockPhysicsEvent event) {
		if (event.getSourceBlock().getType() != Material.AIR && event.getChangedType().toString().contains("RAIL"))
			return;
		if (!isProtected(event.getBlock()) || !isProtected(event.getSourceBlock()))
			return;
		event.setCancelled(true);
	}

	@EventHandler
	public void entityChange(EntityChangeBlockEvent event) {
		if (!isProtected(event.getBlock()))
			return;
		event.setCancelled(true);
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player p : event.getBlock().getLocation().getWorld().getPlayers()) {
					p.sendBlockChange(event.getBlock().getLocation(), event.getBlock().getBlockData());
				}
			}
		}.runTaskLater(ScorchCore.getInstance(), 1);

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void spongeAbsorb(SpongeAbsorbEvent event) {
		if (!isProtected(event.getBlock()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockSpread(BlockSpreadEvent event) {
		if (!isProtected(event.getSource()) && !isProtected(event.getBlock()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void entityExplode(EntityExplodeEvent event) {
		if (!isProtected(event.getEntity()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void entitySpawn(CreatureSpawnEvent event) {
		if (!isProtected(event.getLocation().getBlock()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockFertilize(BlockFertilizeEvent event) {
		UUID player = getResponsible(event.getBlock().getLocation());
		if (player == null)
			return;

		List<Location> locs = tracker.get(player);
		locs.addAll(event.getBlocks().stream()
				.filter(b -> !b.getBlock().getRelative(BlockFace.UP).getType().toString().contains("SAPLING"))
				.map(b -> b.getLocation()).collect(Collectors.toList()));
		tracker.put(player, locs);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void structureGrow(StructureGrowEvent event) {
		UUID player = getResponsible(event.getLocation());
		if (player == null)
			return;

		List<Location> locs = tracker.get(player);
		locs.addAll(event.getBlocks().stream()
				.filter(b -> !b.getBlock().getRelative(BlockFace.UP).getType().toString().contains("SAPLING"))
				.map(b -> b.getLocation()).collect(Collectors.toList()));
		tracker.put(player, locs);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void explosionCreate(ExplosionPrimeEvent event) {
		if (!isProtected(event.getEntity()))
			return;
		event.setRadius(0);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void dropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		if (getStatus(player.getUniqueId()) == BuildStatus.OVERRIDE) {
			event.setCancelled(false);
			return;
		}
	}

	public boolean toggleMode(UUID uuid, BuildStatus status) {
		if (getStatus(uuid) == status) {
			setStatus(uuid, BuildStatus.NONE);
		} else {
			setStatus(uuid, status);
		}
		return getStatus(uuid) == status;
	}

	public void setStatus(UUID uuid, BuildStatus status) {
		Player player = Bukkit.getPlayer(uuid);
		BuildStatus old = getStatus(uuid);
		this.status.put(uuid, status);

		if (player == null)
			return;
		if (status == BuildStatus.NONE) {
			if (player.isFlying()) {
				Block highest = player.getWorld().getHighestBlockAt(player.getLocation());
				int diff = player.getLocation().getBlockY() - highest.getY();
				if (diff >= 3) {
					Location target = highest.getLocation().add(.5, 0, .5);
					target.setPitch(player.getLocation().getPitch());
					target.setYaw(player.getLocation().getYaw());
					player.teleport(target);
				}
			}
			player.setGameMode(GameMode.SURVIVAL);
			player.getInventory().setContents(invTracker.getOrDefault(uuid, new ItemStack[0]));
		} else if (old == BuildStatus.NONE) {
			player.setGameMode(GameMode.CREATIVE);
			invTracker.put(uuid, player.getInventory().getStorageContents());
			player.getInventory().clear();
			tracker.putIfAbsent(uuid, new ArrayList<>());
			entityTracker.putIfAbsent(uuid, new ArrayList<>());
		}
	}

	public BuildStatus getStatus(UUID uuid) {
		return status.getOrDefault(uuid, BuildStatus.NONE);
	}

	public enum BuildStatus {
		NONE, BUILD, INSPECT, OVERRIDE
	}

	private boolean isRedstone(Material mat) {
		List<Material> arrays = Arrays.asList(Material.REDSTONE_BLOCK, Material.REDSTONE_TORCH,
				Material.REDSTONE_WALL_TORCH);
		return arrays.contains(mat);
	}

	private boolean isProtected(Location loc) {
		for (List<Location> locs : tracker.values()) {
			if (locs.contains(loc)) {
				return true;
			}
		}
		return false;
	}

	private boolean isProtected(Block block) {
		return isProtected(block.getLocation());
	}

	private boolean isProtected(Entity ent) {
		for (List<Entity> ents : entityTracker.values()) {
			if (ents.contains(ent)) {
				return true;
			}
		}
		return false;
	}
}
