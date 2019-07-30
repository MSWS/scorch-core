package com.scorch.core.modules.report;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.report.Report.ReportType;
import com.scorch.core.utils.MSG;
import com.scorch.core.utils.Sounds;

public class ReportInventoryListener implements Listener {
	public ReportInventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		ReportModule rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);
		Player player = (Player) event.getWhoClicked();
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		ItemStack item = event.getCurrentItem();

		if (item == null || item.getType() == Material.AIR)
			return;

		ReportType type = null;

		for (ReportType t : ReportType.values())
			if (item.isSimilar(t.getItem())) {
				type = t;
				break;
			}

		if ("report".equals(sp.getTempData("openInventory", String.class))) {
			event.setCancelled(true);

			if (type == null)
				return;

			if (item.getType() == Material.LIME_STAINED_GLASS_PANE)
				return;

			if (!sp.hasTempData("reporting"))
				return;

			UUID target = sp.getTempData("reporting", UUID.class);

			Report report = new Report(player.getUniqueId(), target, type, sp.getTempData("reason", String.class));
			ScorchCore.getInstance().getModule("ReportModule", ReportModule.class).addReport(report);

			MSG.tell(player, "Successfully reported " + (Bukkit.getOfflinePlayer(target).getName()) + " for "
					+ report.getReason());

			player.playSound(player.getLocation(), Sounds.LEVEL_UP.bukkitSound(), 2, 1);

			player.closeInventory();
		}

		if ("reporthandle".equals(sp.getTempData("openInventory", String.class))) {
			event.setCancelled(true);

			if (type == null)
				return;

			final ReportType finalType = type;

			List<Report> reports = rm.getOpenReports().stream().filter(r -> r.getType() == finalType)
					.collect(Collectors.toList());
			Collections.sort(reports);

			if (reports.isEmpty()) {
				MSG.tell(player, "No open reports of that type.");
				player.closeInventory();
				return;
			}

			Report r = reports.get(0);

			MSG.tell(player, "You are now assigned to report " + r.getId());
			player.closeInventory();
		}

	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		for (String id : new String[] { "openInventory", "reporting", "reason" })
			sp.removeTempData(id);
	}

}
