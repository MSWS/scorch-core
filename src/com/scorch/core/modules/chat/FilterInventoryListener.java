package com.scorch.core.modules.chat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.chat.FilterEntry.FilterType;
import com.scorch.core.modules.players.CPlayer;
import com.scorch.core.utils.Sounds;

public class FilterInventoryListener implements Listener {
	public FilterInventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		FilterModule fm = ScorchCore.getInstance().getModule("FilterModule", FilterModule.class);

		Player player = (Player) event.getWhoClicked();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);

		if (!"FilterGUI".equals(cp.getTempData("openInventory")))
			return;

		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR)
			return;

		event.setCancelled(true);

		player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 2, 1);

		int page = cp.getTempInteger("page");

		if (event.getRawSlot() == event.getInventory().getSize() - 1) {
			openFilter(player, page + 1);
			return;
		}

		if (event.getRawSlot() == event.getInventory().getSize() - 9) {
			openFilter(player, page - 1);
			return;
		}

		int i = page * (event.getInventory().getSize() - 9) + event.getRawSlot();

		FilterEntry entry = fm.getEntries().get(i);
		FilterType next = FilterType.values()[entry.getType().ordinal() - 1 < 0 ? FilterType.values().length - 1
				: entry.getType().ordinal() - 1],
				prev = FilterType.values()[(entry.getType().ordinal() + 1) % FilterType.values().length];

		switch (event.getClick()) {
		case DROP:
			fm.removeWord(entry);
			break;
		case LEFT:
			entry.setType(prev);
			break;
		case RIGHT:
			entry.setType(next);
			break;
		default:
			player.playSound(player.getLocation(), Sounds.VILLAGER_NO.bukkitSound(), 2, 1);
			break;
		}

		openFilter(player, page);
	}

	private void openFilter(Player player, int page) {
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		player.openInventory(ScorchCore.getInstance().getModule("FilterModule", FilterModule.class).getFilterGUI(page));
		cp.setTempData("openInventory", "FilterGUI");
		cp.setTempData("page", page);
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		for (String id : new String[] { "page", "openInventory" })
			cp.removeTempData(id);
	}

}
