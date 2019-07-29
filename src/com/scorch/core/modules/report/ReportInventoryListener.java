package com.scorch.core.modules.report;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.CPlayer;
import com.scorch.core.modules.report.Report.ReportType;
import com.scorch.core.utils.MSG;
import com.scorch.core.utils.Sounds;

public class ReportInventoryListener implements Listener {
	public ReportInventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();

		CPlayer cp = ScorchCore.getInstance().getPlayer(player);

		ItemStack item = event.getCurrentItem();

		if (item == null || item.getType() == Material.AIR)
			return;

		if (!"report".equals(cp.getTempString("openInventory")))
			return;

		event.setCancelled(true);

		if (item.getType() == Material.LIME_STAINED_GLASS_PANE)
			return;

		if (!cp.hasTempData("reporting"))
			return;

		UUID target = cp.getTempData("reporting", UUID.class);

		ReportType type = null;

		for (ReportType t : ReportType.values())
			if (item.isSimilar(t.getItem())) {
				type = t;
				break;
			}

		if (type == null)
			return;

		Report report = new Report(player.getUniqueId(), target, type, cp.getTempString("reason"));
		ScorchCore.getInstance().getModule("ReportModule", ReportModule.class).addReport(report);

		MSG.tell(player,
				"Successfully reported " + (Bukkit.getOfflinePlayer(target).getName()) + " for " + report.getReason());

		player.playSound(player.getLocation(), Sounds.LEVEL_UP.bukkitSound(), 2, 1);

		player.closeInventory();
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		for (String id : new String[] { "openInventory", "reporting", "reason" })
			cp.removeTempData(id);
	}

}
