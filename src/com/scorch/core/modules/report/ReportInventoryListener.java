package com.scorch.core.modules.report;

import java.io.IOException;
import java.util.ArrayList;
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
import com.scorch.core.pastebin.Paste;
import com.scorch.core.pastebin.Paste.Expire;
import com.scorch.core.pastebin.Paste.Language;
import com.scorch.core.pastebin.Paste.PasteResult;
import com.scorch.core.pastebin.Paste.Visibility;
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
			if (type == ReportType.CHAT) {
				List<String> lines = rm.getLogs(player.getUniqueId());
				List<String> f = new ArrayList<String>();
				f.add(ScorchCore.getInstance().getName() + " chat logs for " + MSG.camelCase(type.toString())
						+ " report against " + Bukkit.getPlayer(target).getName() + " (" + target + ")" + " for "
						+ report.getReason());
				f.add("[WARNING] Chat logs are for staff eyes only, any leaking, sharking, or other malicious intent used by this service is unallowed and will result in disciplinary actions.");
				f.add(" ");
				if (report.getServer() != null)
					f.add("Server: " + report.getServer());
				f.add("====================================");
				f.add("BEGIN CHAT LOGS (Report ID: " + report.getId() + ")");
				f.add("====================================");
				f.addAll(lines);
				f.add("====================================");
				f.add("END OF CHAT LOGS (Report ID: " + report.getId() + ")");
				f.add("====================================");

				Paste paste = new Paste(String.join("\n", f), report.getId() + " chat logs", Visibility.UNLISTED,
						Expire.NEVER, Language.TEXT);

				try {
					PasteResult result = paste.upload();
					if (result.isValid())
						report.setPastebin(result.getPasteURL());
				} catch (IOException e) {
					e.printStackTrace();
					MSG.tell(player, "An error occured reporting this player.");
					return;
				}
			}

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

			r.handle(player.getName(), "Assigned");

			MSG.tell(player, "You are now assigned to report " + r.getId());

			sp.setData("assignedreport", r.getId());
			player.closeInventory();
			rm.sendReportInfo(player);
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
