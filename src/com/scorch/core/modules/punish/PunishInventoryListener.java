package com.scorch.core.modules.punish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.CPlayer;
import com.scorch.core.utils.MSG;

public class PunishInventoryListener implements Listener {
	public PunishInventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		if (!"punish".equals(cp.getTempData("openInventory")))
			return;

		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
			return;

		event.setCancelled(true);

		String id = "";
		for (String res : ScorchCore.getInstance().getGui().getConfigurationSection("punish").getKeys(false)) {
			if (ScorchCore.getInstance().getGui().getInt("punish." + res + ".Slot") == event.getRawSlot()) {
				id = res;
				break;
			}
		}

		if (ScorchCore.getInstance().getGui().contains("punish." + id + ".Type")) {
			
		}

		MSG.tell(player, id);

	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		for (String id : new String[] { "punishing", "reason", "page" })
			cp.removeTempData(id);
	}

}
