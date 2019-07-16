package com.scorch.core.modules.punish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.scorch.core.ScorchCore;
import com.scorch.core.commands.HistoryCommand;
import com.scorch.core.commands.PunishCommand;
import com.scorch.core.commands.UnpunishCommand;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.DataManager;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.MSG;
import com.scorch.core.utils.Utils;

/**
 * {@link PunishModule} Manages and loads all commands, events, etc. related to
 * the punishment system.
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

	private List<Punishment> punishments;

	private Map<UUID, List<Punishment>> linked;

	@Override
	public void initialize() {
		new PunishCommand();
		new HistoryCommand();
		new UnpunishCommand();

		joinListener = new PunishLoginListener();
		clickListener = new PunishInventoryListener();

		refreshPunishments();
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

		linked.put(punishment.getTargetUUID(), current);

		ScorchCore.getInstance().getDataManager().saveObject("punishments", punishment);
	}

	public Inventory getPunishGUI(OfflinePlayer player) {
		Inventory inv = Utils.getGui(player, ScorchCore.getInstance().getGui(), "punish", 0);
		List<Punishment> history = getPunishments(player.getUniqueId());
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

	public Inventory getHistoryGUI(OfflinePlayer player) {
		return null;
	}

	public List<Punishment> getPunishments(UUID player) {
		return linked.getOrDefault(player, new ArrayList<>());
	}

	public void refreshPunishments() {
		punishments = new ArrayList<Punishment>();
		linked = new HashMap<UUID, List<Punishment>>();

		try {
			ScorchCore.getInstance().getDataManager().createTable(table, Punishment.class);

			ScorchCore.getInstance().getDataManager().getAllObjects("punishments").forEach(punish -> {
				Punishment p = (Punishment) punish;
				punishments.add(p);

				List<Punishment> current = linked.getOrDefault(p.getTargetUUID(), new ArrayList<>());
				current.add(p);

				linked.put(p.getTargetUUID(), current);
			});
		} catch (NoDefaultConstructorException | DataObtainException e) {
			e.printStackTrace();
		}
	}
}
