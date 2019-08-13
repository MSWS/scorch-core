package com.scorch.core.modules.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.staff.BuildModeModule;
import com.scorch.core.modules.staff.DamageEntry;
import com.scorch.core.utils.MSG;

public class PlayerCombatModule extends AbstractModule implements Listener {

	@SuppressWarnings("unused")
	private BuildModeModule bm;

	private Map<UUID, List<DamageEntry>> damageHistory;

	private boolean oldPvp = true;

	private Listener legacyListener;

	public PlayerCombatModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		bm = ScorchCore.getInstance().getModule("BuildModeModule", BuildModeModule.class);

		damageHistory = new HashMap<UUID, List<DamageEntry>>();

		if (oldPvp && !Bukkit.getVersion().contains("1.8")) {
			legacyListener = new LegacyHitListener();
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1024);
				p.saveData();
			}
		}

		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@Override
	public void disable() {
		EntityDamageEvent.getHandlerList().unregister(this);

		if (legacyListener != null)
			PlayerJoinEvent.getHandlerList().unregister(legacyListener);
	}

	public List<DamageEntry> getDamageEntries(UUID uuid) {
		return damageHistory.getOrDefault(uuid, new ArrayList<DamageEntry>());
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (event instanceof EntityDamageByBlockEvent || event instanceof EntityDamageByEntityEvent)
			return;

		Entity ent = event.getEntity();

		String name = "";

		switch (event.getCause()) {
		case BLOCK_EXPLOSION:
			break;
		case CONTACT:
			name = "Cactus";
			break;
		case CRAMMING:
			name = "Entity Cramming";
			break;
		case FALL:
			name = "Fall Damage";
			break;
		case FIRE_TICK:
			name = "Burning";
			break;
		case FLY_INTO_WALL:
			name = "";
			break;
		case HOT_FLOOR:
			name = "Magma";
			break;
		case MAGIC:
			name = "Potion";
			break;
		case VOID:
			name = "Void Damage";
			break;
		case WITHER:
			name = "Wither Effect";
			break;
		default:
			name = MSG.camelCase(event.getCause().toString());
			break;
		}
		List<DamageEntry> entries = getDamageEntries(ent.getUniqueId());
		entries.add(new DamageEntry(name, event.getDamage()));
		damageHistory.put(event.getEntity().getUniqueId(), entries);
	}

	@EventHandler
	public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		Entity damaged = event.getEntity();
		if (damager == null || damaged == null)
			return;
		if (damager instanceof Projectile) {
			if (((Projectile) damager).getShooter() == null)
				return;
			if (!(((Projectile) damager).getShooter() instanceof Entity))
				return;
			damager = (Entity) (((Projectile) damager).getShooter());
		}

		String name = MSG.camelCase(damager.getType().toString()), weapon = "Unknown";

		if (event.getDamager() instanceof Projectile) {
			switch (event.getDamager().getType()) {
			case ARROW:
			case TIPPED_ARROW:
			case SPECTRAL_ARROW:
				weapon = "Archery";
				break;
			default:
				weapon = MSG.camelCase(event.getDamager().getType().toString());
				break;
			}
		}

		if (damager.getCustomName() != null) {
			name = damager.getCustomName();
		} else if (damager instanceof Player) {
			name = damager.getName();
			ItemStack hand = ((Player) damager).getInventory().getItemInMainHand();
			if (hand.hasItemMeta() && hand.getItemMeta().hasDisplayName()) {
				weapon = hand.getItemMeta().getDisplayName();
			} else if (hand.getType() == Material.AIR) {
				weapon = "Hand";
			} else {
				weapon = hand.getType().toString();
			}
		}

		List<DamageEntry> entries = getDamageEntries(damaged.getUniqueId());
		entries.add(new DamageEntry(name, event.getDamage(), weapon));
		damageHistory.put(damaged.getUniqueId(), entries);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();

		List<DamageEntry> entries = getDamageEntries(player.getUniqueId());

		double total = 0, avg = 0;

		long time = !entries.isEmpty() ? entries.get(0).getTimeElapsed() : System.currentTimeMillis();

		Map<String, Double> credit = new HashMap<>();

		for (DamageEntry e : entries) {
			total += e.getDamageAmount();
			avg += e.getDamageAmount();
			credit.put(e.getDamager(), credit.getOrDefault(e.getDamager(), 0.0) + e.getDamageAmount());
		}

		avg /= entries.size();
		MSG.tell(player, " ");

		MSG.tell(player, "&cDeath Statistics &m&l>>>&4 " + total + " &7damage over &e" + MSG.getTime(time)
				+ "&7 (Avg: &e" + MSG.parseDecimal(avg, 2) + "/hit&7)");

		int pos = 1;

		for (DamageEntry e : entries) {
			MSG.tell(player, "&e" + pos + "&7: " + e.format());
			pos++;
		}
		MSG.tell(player, " ");

		for (Entry<String, Double> c : credit.entrySet()) {
			MSG.tell(player, "&a" + c.getKey() + " &7dealt &e" + c.getValue() + " &7damage total (&b"
					+ MSG.parseDecimal(c.getValue() / total * 100.0, 2) + "%&7)");
		}

		MSG.tell(player, " ");

		getDamageEntries(player.getUniqueId()).clear();
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		getDamageEntries(player.getUniqueId()).clear();
	}

}
