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

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.CPlayer;

public class PunishInventoryListener implements Listener {
	public PunishInventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);

		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
			return;

		OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(cp.getTempString("punishing").split("\\|")[0]));
		List<Punishment> history = ScorchCore.getInstance().getPunishModule().getPunishments(target.getUniqueId());

		if ("viewing".equals(cp.getTempString("openInventory"))) {
			int page = cp.getTempInteger("page");
			event.setCancelled(true);

			if (event.getRawSlot() == event.getInventory().getSize() - 1) {
				cp.setTempData("page", page + 1);
				refreshHistory(player, target);
				return;
			} else if (event.getRawSlot() == event.getInventory().getSize() - 9) {
				cp.setTempData("page", page - 1);
				refreshHistory(player, target);
				return;
			}

			if (event.getClickedInventory().getType() != InventoryType.CHEST)
				return;

			if ((event.getClick() == ClickType.RIGHT && cp.hasTempData("reason")
					&& player.hasPermission("scorch.punish.remove"))
					|| (event.getClick() == ClickType.SHIFT_LEFT && player.hasPermission("scorch.punish.delete"))) {
				int i = page * (event.getInventory().getSize() - 9) + event.getRawSlot();
				Punishment p = history.get(i);

				switch (event.getClick()) {
				case RIGHT:
					if (!p.isActive())
						return;
					p.remove(player.getName(), cp.getTempString("reason"));
					return;
				case SHIFT_LEFT:
					ScorchCore.getInstance().getPunishModule().deletePunishment(p);
					return;
				default:
					return;
				}

			}
			return;
		}

		if (!"punish".equals(cp.getTempData("openInventory")))
			return;

		event.setCancelled(true);

		if (event.getClickedInventory().getType() != InventoryType.CHEST)
			return;

		String id = "";
		for (String res : ScorchCore.getInstance().getGui().getConfigurationSection("punish").getKeys(false)) {
			if (ScorchCore.getInstance().getGui().getInt("punish." + res + ".Slot") == event.getRawSlot()) {
				id = res;
				break;
			}
		}

		if (event.getRawSlot() == event.getInventory().getSize() - 1) {
			cp.setTempData("page", 0);
			refreshHistory(player, target);
			return;
		}

		if (ScorchCore.getInstance().getGui().contains("punish." + id + ".Type")) {
			Punishment punishment = new Punishment(target.getUniqueId(), player.getName(), cp.getTempString("reason"),
					System.currentTimeMillis(), ScorchCore.getInstance().getGui().getLong("punish." + id + ".Duration"),
					PunishType.valueOf(ScorchCore.getInstance().getGui().getString("punish." + id + ".Type")));

			ScorchCore.getInstance().getPunishModule().addPunishment(punishment);

			player.closeInventory();
		}

		if ((event.getClick() == ClickType.RIGHT && player.hasPermission("scorch.punish.remove"))
				|| (event.getClick() == ClickType.SHIFT_LEFT && player.hasPermission("scorch.punish.delete"))) {
			Collections.sort(history);
			int i = (event.getRawSlot() + 1) / 9 - 1;

			Punishment p = history.get(i);

			switch (event.getClick()) {
			case RIGHT:
				if (!p.isActive())
					return;
				p.remove(player.getName(), cp.getTempString("reason"));
				break;
			case SHIFT_LEFT:
				ScorchCore.getInstance().getPunishModule().deletePunishment(p);
				refreshHistory(player, target);
				break;
			default:
				break;
			}

			refreshPunish(player, target);
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		for (String id : new String[] { "punishing", "reason", "page", "openInventory" })
			cp.removeTempData(id);
	}

	private void refreshHistory(Player player, OfflinePlayer target) {
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		String tempPunish = cp.getTempString("punishing");
		String tempReason = cp.getTempString("reason");
		int tempPage = cp.getTempInteger("page");
		player.openInventory(
				ScorchCore.getInstance().getPunishModule().getHistoryGUI(target, cp.getTempInteger("page")));
		cp.setTempData("openInventory", "viewing");
		cp.setTempData("punishing", tempPunish);
		cp.setTempData("reason", tempReason);
		cp.setTempData("page", tempPage);
	}

	private void refreshPunish(Player player, OfflinePlayer target) {
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		String tempPunish = cp.getTempString("punishing");
		String tempReason = cp.getTempString("reason");
		player.openInventory(ScorchCore.getInstance().getPunishModule().getPunishGUI(player, target));
		cp.setTempData("openInventory", "punish");
		cp.setTempData("punishing", tempPunish);
		cp.setTempData("reason", tempReason);
	}

}