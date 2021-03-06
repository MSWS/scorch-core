package com.scorch.core.modules.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.report.Report.ReportType;
import com.scorch.core.modules.report.Report.ResolutionType;
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

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		ReportModule rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);
		Player player = (Player) event.getWhoClicked();
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());

		ItemStack item = event.getCurrentItem();

		if (item == null || item.getType() == Material.AIR)
			return;

		ReportType type = null;
		ResolutionType res = null;

		for (ReportType t : ReportType.values())
			if (item.isSimilar(t.getItem())) {
				type = t;
				break;
			}

		for (ResolutionType t : ResolutionType.values()) {
			if (item.isSimilar(t.getItem())) {
				res = t;
				break;
			}
		}

		if ("reporthistory".equals(sp.getTempData("openInventory", String.class))) {
			event.setCancelled(true);
			int page = sp.getTempData("page", Integer.class, 0);

			OfflinePlayer target = Bukkit.getOfflinePlayer(sp.getTempData("viewing", String.class));

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
			report.setServer(ScorchCore.getInstance().getServerName());
			if (type == ReportType.CHAT) {
				List<String> lines = rm.getLogs(player.getUniqueId());
				if (lines.isEmpty()) {
					MSG.cTell(player, "nomessages");
					return;
				}

				List<String> f = new ArrayList<String>();
				f.add(ScorchCore.getInstance().getName() + " chat logs for " + MSG.camelCase(type.toString())
						+ " report against " + Bukkit.getPlayer(target).getName() + " (" + target + ")");
				f.add("Reason: " + report.getReason());
				f.add("");
				f.add("[WARNING] Chat logs are for staff eyes only.");
				f.add("");
				f.add("Chat Log Hash: " + MSG.hashWithSalt(report.getId(), report.getReason(), 16, 5));
				f.add("(Confirm this with '/confirmreport " + report.getId() + " " + report.getReason() + "')");
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

			String msg = ScorchCore.getInstance().getMessage("reportcreated");

			msg = msg.replace("%id%", report.getId()).replace("%player%", Bukkit.getOfflinePlayer(target).getName());

			MSG.tell(player, msg);

			player.playSound(player.getLocation(), Sounds.LEVEL_UP.bukkitSound(), 2, 1);

			player.closeInventory();
		}

		if ("reporthandle".equals(sp.getTempData("openInventory", String.class))) {
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 2, 1);

			if (type == null)
				return;

			final ReportType finalType = type;

			List<Report> reports = rm.getOpenReports().stream().filter(r -> r.getType() == finalType)
					.collect(Collectors.toList());
			Collections.sort(reports);

			if (reports.isEmpty()) {
				MSG.cTell(player, "noreports");
				player.closeInventory();
				return;
			}

			Report r = reports.get(0);

			MSG.tell(player, ScorchCore.getInstance().getMessage("reportassigned").replace("%id%", r.getId()));

			sp.setData("assignedreport", r.getId());
			player.closeInventory();
			rm.sendReportInfo(player);
		}

		if ("reportclose".equals(sp.getTempData("openInventory", String.class))) {
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 2, 1);

			if (res == null)
				return;
			Report report = rm.getReport(sp.getData("assignedreport", String.class));
			String reason = sp.getTempData("closereason", String.class);

			if (res == ResolutionType.CANCEL) {
				MSG.tell(player, "Cancelled handling of report.");
				sp.removeData("assignedreport");
				player.closeInventory();
				return;
			}

			report.resolve(player.getName(), res, reason);
			sp.removeData("assignedreport");
			String msg = ScorchCore.getInstance().getMessage("resolvereport");
			msg = msg.replace("%id%", report.getId()).replace("%reason%", reason).replace("%status%",
					MSG.camelCase(res + ""));
			MSG.tell(player, msg);
			if (res == ResolutionType.CONFIRMED) {
				ScorchCore.getInstance().getPunishModule().openPunishGUI(player,
						Bukkit.getOfflinePlayer(report.getTarget()), "Report #" + report.getId() + " - " + reason);
				return;
			}

			player.closeInventory();
			rm.updateReport(report);
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		for (String id : new String[] { "openInventory", "reporting", "reason", "closereason" })
			sp.removeTempData(id);
	}

	private void refreshHistory(Player player, OfflinePlayer target) {
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		String tempViewing = sp.getTempData("viewing", String.class);
		int tempPage = sp.getTempData("page", Integer.class, 0);
		player.openInventory(
				ScorchCore.getInstance().getModule("ReportModule", ReportModule.class).getReportsGUI(target, tempPage));
		sp.setTempData("openInventory", "reporthistory");
		sp.setTempData("viewing", tempViewing);
		sp.setTempData("page", tempPage);
	}

}
