package com.scorch.core.modules.punish;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.staff.TrustModule;
import com.scorch.core.utils.MSG;
import com.scorch.core.utils.Sounds;
import com.scorch.core.utils.Utils;

/**
 * Manages Punish GUI
 * 
 * @author imodm
 *
 */
public class PunishInventoryListener implements Listener {
	public PunishInventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		PunishModule pm = ScorchCore.getInstance().getPunishModule();

		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		ItemStack item = event.getCurrentItem();

		if (item == null || item.getType() == Material.AIR)
			return;

		if ("trust".equals(sp.getTempData("openInventory"))) {
			event.setCancelled(true);
			return;
		}

		if (!sp.hasTempData("punishing"))
			return;

		OfflinePlayer target = Bukkit
				.getOfflinePlayer(UUID.fromString(sp.getTempData("punishing", String.class).split("\\|")[0]));
		List<Punishment> history = ScorchCore.getInstance().getPunishModule().getPunishments(target.getUniqueId());

		if ("viewing".equals(sp.getTempData("openInventory", String.class))) {
			int page = sp.getTempData("page", Integer.class, 0);
			event.setCancelled(true);

			if (event.getClickedInventory().getType() != InventoryType.CHEST)
				return;

			player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 2, 1);

			if (event.getRawSlot() == event.getInventory().getSize() - 1) {
				sp.setTempData("page", page + 1);
				refreshHistory(player, target);
				return;
			} else if (event.getRawSlot() == event.getInventory().getSize() - 9) {
				sp.setTempData("page", page - 1);
				refreshHistory(player, target);
				return;
			}

			if ((event.getClick() == ClickType.RIGHT && sp.hasTempData("reason")
					&& player.hasPermission("scorch.punish.remove"))
					|| (event.getClick() == ClickType.SHIFT_LEFT && player.hasPermission("scorch.punish.delete"))) {
				int i = page * (event.getInventory().getSize() - 9) + event.getRawSlot();
				Punishment p = history.get(i);

				switch (event.getClick()) {
				case RIGHT:
					if (!p.isActive())
						return;
					confirm(player, p, "Confirm punishment &cremoval", "remove");
					return;
				case SHIFT_LEFT:
					confirm(player, p, "Confirm punishment &4deletion", "delete");
					return;
				default:
					return;
				}

			}
			return;
		}

		if ("confirm".equals(sp.getTempData("openInventory"))) {
			event.setCancelled(true);
			if (event.getClickedInventory().getType() != InventoryType.CHEST)
				return;
			if (item.getType() == Material.GREEN_WOOL) {
				Punishment p = pm.getPunishment(sp.getTempData("punishremove", UUID.class));
				String type = sp.getTempData("confirming", String.class);
				String tempReason = sp.getTempData("reason", String.class);
				if (type.equals("remove")) {
					p.remove(player.getName(), tempReason);
					player.playSound(player.getLocation(), Sounds.VILLAGER_TRADE.bukkitSound(), 2, 1);
					player.closeInventory();
				} else if (type.equals("delete")) {
					ScorchCore.getInstance().getPunishModule().deletePunishment(p);
					player.playSound(player.getLocation(), Sounds.VILLAGER_YES.bukkitSound(), 2, 1);
					player.closeInventory();
				}
			} else if (item.getType() == Material.RED_WOOL) {
				player.playSound(player.getLocation(), Sounds.VILLAGER_NO.bukkitSound(), 2, 1);
				player.closeInventory();
			}
			return;
		}

		if ("punishmentrecord".equals(sp.getTempData("openInventory"))) {
			int page = sp.getTempData("page", Integer.class, 0);
			event.setCancelled(true);

			if (event.getClickedInventory().getType() != InventoryType.CHEST)
				return;

			player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 2, 1);

			if (event.getRawSlot() == event.getInventory().getSize() - 1) {
				sp.setTempData("page", page + 1);
				refreshHistory(player, target);
				return;
			} else if (event.getRawSlot() == event.getInventory().getSize() - 9) {
				sp.setTempData("page", page - 1);
				refreshHistory(player, target);
				return;
			}
		}

		if (!"punish".equals(sp.getTempData("openInventory")))
			return;

		event.setCancelled(true);

		if (event.getClickedInventory().getType() != InventoryType.CHEST)
			return;

		player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 2, 1);

		String id = "";
		for (String res : ScorchCore.getInstance().getGui().getConfigurationSection("punish").getKeys(false)) {
			if (ScorchCore.getInstance().getGui().getInt("punish." + res + ".Slot") == event.getRawSlot()) {
				if (item.getType().toString()
						.equals(ScorchCore.getInstance().getGui().getString("punish." + res + ".Icon"))) {
					id = res;
					break;
				}
			}
		}

		if (id.equals("trust") && player.hasPermission("scorch.punish.viewtrust")) {
			player.openInventory(ScorchCore.getInstance().getModule("TrustModule", TrustModule.class)
					.getInventory(target.getUniqueId()));
			sp.setTempData("openInventory", "trust");
			return;
		}

		if (event.getRawSlot() == event.getInventory().getSize() - 1) {
			sp.setTempData("page", 0);
			refreshHistory(player, target);
			return;
		}

		if (ScorchCore.getInstance().getGui().contains("punish." + id + ".Type")) {
			Punishment punishment = new Punishment(target.getUniqueId(), player.getName(),
					sp.getTempData("reason", String.class), System.currentTimeMillis(),
					ScorchCore.getInstance().getGui().getLong("punish." + id + ".Duration"),
					PunishType.valueOf(ScorchCore.getInstance().getGui().getString("punish." + id + ".Type")));

			ScorchCore.getInstance().getPunishModule().addPunishment(punishment);
			player.playSound(player.getLocation(), Sounds.NOTE_BASS.bukkitSound(), 2, 2);
			player.closeInventory();
		}

		if ((event.getClick() == ClickType.RIGHT && player.hasPermission("scorch.punish.remove"))
				|| (event.getClick() == ClickType.SHIFT_LEFT && player.hasPermission("scorch.punish.delete"))) {
			Collections.sort(history);
			int i = (event.getRawSlot() + 1) / 9 - 1;

			if ((event.getRawSlot() - 8) % 9 != 0)
				return;

			if (event.getRawSlot() < 0 || i < 0 || i > history.size())
				return;

			Punishment p = history.get(i);

			switch (event.getClick()) {
			case RIGHT:
				if (!p.isActive())
					return;
				confirm(player, p, "Confirm punishment &cremoval", "remove");
				return;
			case SHIFT_LEFT:
				confirm(player, p, "Confirm punishment &4deletion", "delete");
				return;
			default:
				break;
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		for (String id : new String[] { "punishing", "reason", "page", "openInventory", "trustenum" })
			sp.removeTempData(id);
	}

	private void refreshHistory(Player player, OfflinePlayer target) {
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		String tempPunish = sp.getTempData("punishing", String.class);
		String tempReason = sp.getTempData("reason", String.class);
		int tempPage = sp.getTempData("page", Integer.class, 0);
		player.openInventory(ScorchCore.getInstance().getPunishModule().getHistoryGUI(target,
				sp.getTempData("page", Integer.class, 0)));
		sp.setTempData("openInventory", "viewing");
		sp.setTempData("punishing", tempPunish);
		sp.setTempData("reason", tempReason);
		sp.setTempData("page", tempPage);
	}

	public void refreshRecord(Player player, OfflinePlayer target) {
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		String tempPunish = sp.getTempData("punishing", String.class);
		int tempPage = sp.getTempData("page", Integer.class, 0);
		player.openInventory(ScorchCore.getInstance().getPunishModule().getRecordGUI(target,
				sp.getTempData("page", Integer.class, 0)));
		sp.setTempData("openInventory", "viewing");
		sp.setTempData("punishing", tempPunish);
		sp.setTempData("page", tempPage);
	}

	private void confirm(Player player, Punishment punishment, String message, String removeType) {
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		Inventory inv = Bukkit.createInventory(null, 54, MSG.color(message));
		inv.setItem(13, punishment.getItem());

		ItemStack confirm = Utils.parseItem(ScorchCore.getInstance().getGui(), "confirm", player),
				cancel = Utils.parseItem(ScorchCore.getInstance().getGui(), "cancel", player);

		for (int y = 2; y < 5; y++) {
			for (int x = 1; x < 4; x++) {
				int slot = 8 + (y * 9) + x;
				inv.setItem(slot, confirm);
			}
			for (int x = 7; x < 10; x++) {
				int slot = 8 + (y * 9) + x;
				inv.setItem(slot, cancel);
			}
		}

		String tempPunish = sp.getTempData("punishing", String.class);
		String tempReason = sp.getTempData("reason", String.class);

		player.openInventory(inv);
		sp.setTempData("punishremove", punishment.getId());
		sp.setTempData("openInventory", "confirm");
		sp.setTempData("confirming", removeType);
		sp.setTempData("punishing", tempPunish);
		sp.setTempData("reason", tempReason);
	}

}
