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
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.Sounds;

public class FilterInventoryListener implements Listener {
	public FilterInventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		FilterModule fm = ScorchCore.getInstance().getModule("FilterModule", FilterModule.class);

		Player player = (Player) event.getWhoClicked();
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		if (!"FilterGUI".equals(sp.getTempData("openInventory")))
			return;

		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR)
			return;

		event.setCancelled(true);

		player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 2, 1);

		int page = sp.getTempData("page", Integer.class, 0);

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
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		player.openInventory(ScorchCore.getInstance().getModule("FilterModule", FilterModule.class).getFilterGUI(page));
		sp.setTempData("openInventory", "FilterGUI");
		sp.setTempData("page", page);
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(event.getPlayer().getUniqueId());
		for (String id : new String[] { "page", "openInventory" })
			sp.removeTempData(id);
	}

}
