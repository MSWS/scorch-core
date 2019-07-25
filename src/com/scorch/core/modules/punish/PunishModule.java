package com.scorch.core.modules.punish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.DataManager;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.exceptions.DataDeleteException;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.MSG;
import com.scorch.core.utils.Utils;

/**
 * {@link PunishModule} Manages and loads events and entries related to the
 * punishment system.
 * 
 * Note that the {@link DataManager} module should be loaded before this.
 * 
 * @author imodm
 *
 */
public class PunishModule extends AbstractModule {
	public PunishModule(String id) {
		super(id);
	}

	private Listener joinListener, clickListener;
	private final String table = "punishments";

	private List<Punishment> punishments, globalPunishments;

	private Map<UUID, List<Punishment>> linked;

	@Override
	public void initialize() {
		joinListener = new PunishLoginListener();
		clickListener = new PunishInventoryListener();

		refreshPunishments();

		new BukkitRunnable() {
			@Override
			public void run() {
				refreshPunishments();
			}
		}.runTaskTimer(ScorchCore.getInstance(), 6000, 6000);
	}

	@Override
	public void disable() {
		PlayerLoginEvent.getHandlerList().unregister(joinListener);
		PlayerLoginEvent.getHandlerList().unregister(clickListener);
	}

	public void addPunishment(Punishment punishment) {
		punishment.execute();

		List<Punishment> current = linked.getOrDefault(punishment.getTargetUUID(), new ArrayList<>());
		current.add(punishment);
		punishments.add(punishment);

		if (punishment.getType() == PunishType.BLACKLIST || punishment.getType() == PunishType.IP_BAN)
			globalPunishments.add(punishment);

		linked.put(punishment.getTargetUUID(), current);

		ScorchCore.getInstance().getDataManager().saveObject("punishments", punishment);
	}

	public Inventory getPunishGUI(Player punisher, OfflinePlayer player) {
		Inventory inv = Utils.getGui(punisher, ScorchCore.getInstance().getGui(), "punish", 0);
		List<Punishment> history = getPunishments(player.getUniqueId());
		Collections.sort(history);
		
		for (int i = 0; i < 5 && i < history.size(); i++) {
			Punishment p = history.get(i);
			ItemStack item = p.getItem();
			inv.setItem(8 + (i * 9), item);
			if (i >= 4) {
				ItemStack more = new ItemStack(Material.BOOK, history.size());
				ItemMeta mMeta = more.getItemMeta();
				mMeta.setDisplayName(MSG.color("&a&l" + history.size() + " Total Punishments"));
				more.setItemMeta(mMeta);
				inv.setItem(inv.getSize() - 1, more);
				break;
			}
		}
		return inv;
	}

	public Inventory getHistoryGUI(OfflinePlayer target, int page) {
		Inventory hist = Bukkit.createInventory(null, 54,
				target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s") + " History");

		List<Punishment> history = ScorchCore.getInstance().getPunishModule().getPunishments(target.getUniqueId());
		Collections.sort(history);

		int slot = 0;

		for (int i = (page * (hist.getSize() - 9)); i < (page * (hist.getSize() - 9)) + hist.getSize() - 9
				&& i < history.size(); i++) {
			Punishment p = history.get(i);
			hist.setItem(slot, p.getItem());
			slot++;
		}

		if (page > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ItemMeta bMeta = back.getItemMeta();
			bMeta.setDisplayName(MSG.color("&aPrevious Page"));
			back.setItemMeta(bMeta);
			hist.setItem(hist.getSize() - 9, back);
		}
		if (hist.getItem(hist.getSize() - 10) != null) {
			ItemStack next = new ItemStack(Material.ARROW);
			ItemMeta nMeta = next.getItemMeta();
			nMeta.setDisplayName(MSG.color("&aNext Page"));
			next.setItemMeta(nMeta);
			hist.setItem(hist.getSize() - 1, next);
		}
		return hist;
	}

	public List<Punishment> getPunishments(UUID player) {
		return linked.getOrDefault(player, new ArrayList<>());
	}

	public void deletePunishment(Punishment p) {
		punishments.remove(p);
		List<Punishment> active = linked.get(p.getTargetUUID());
		active.remove(p);
		linked.put(p.getTargetUUID(), active);
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					ScorchCore.getInstance().getDataManager().deleteObject("punishments",
							new SQLSelector("id", p.getId() + ""));

				} catch (DataDeleteException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
	}

	public void refreshPunishments() {
		punishments = new ArrayList<Punishment>();
		globalPunishments = new ArrayList<>();
		linked = new HashMap<UUID, List<Punishment>>();

		new BukkitRunnable() {
			@Override
			public void run() {
				Logger.log("&9Loading punishments...");
				try {
					ScorchCore.getInstance().getDataManager().createTable(table, Punishment.class);

					ScorchCore.getInstance().getDataManager().getAllObjects("punishments").forEach(punish -> {
						Punishment p = (Punishment) punish;
						punishments.add(p);
						if (p.getType() == PunishType.BLACKLIST || p.getType() == PunishType.IP_BAN)
							globalPunishments.add(p);

						List<Punishment> current = linked.getOrDefault(p.getTargetUUID(), new ArrayList<>());
						current.add(p);

						linked.put(p.getTargetUUID(), current);
					});
				} catch (NoDefaultConstructorException | DataObtainException e) {
					e.printStackTrace();
				}
				Logger.log("&aSuccessfully loaded &e" + punishments.size() + " punishment"
						+ (punishments.size() == 1 ? "" : "s") + "&a.");
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
	}

	public List<Punishment> getGlobalPunishments() {
		return globalPunishments;
	}
}
