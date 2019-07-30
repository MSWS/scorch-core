package com.scorch.core.modules.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.report.Report.ReportType;
import com.scorch.core.modules.report.Report.ResolutionType;
import com.scorch.core.utils.MSG;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ReportModule extends AbstractModule {

	private List<Report> reports;

	private Listener listener;
	private ReportChatListener chatListener;

	private BukkitRunnable messenger;

	public ReportModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		reports = new ArrayList<Report>();

		try {
			ScorchCore.getInstance().getDataManager().createTable("reports", Report.class);

			ScorchCore.getInstance().getDataManager().getAllObjects("reports")
					.forEach(report -> reports.add((Report) report));
		} catch (NoDefaultConstructorException | DataObtainException e) {
			e.printStackTrace();
		}

		listener = new ReportInventoryListener();
		chatListener = new ReportChatListener();

		(messenger = sendReportMessages()).runTaskTimer(ScorchCore.getInstance(), 0, 600);
	}

	@Override
	public void disable() {
		InventoryClickEvent.getHandlerList().unregister(listener);
		InventoryCloseEvent.getHandlerList().unregister(listener);
		AsyncPlayerChatEvent.getHandlerList().unregister(chatListener);
		messenger.cancel();
		reports.clear();
	}

	public List<Report> getReports(UUID player) {
		return reports.parallelStream().filter(r -> r.getReporter().equals(player)).collect(Collectors.toList());
	}

	public List<Report> getReportsAgainst(UUID player) {
		return reports.parallelStream().filter(r -> r.getTarget().equals(player)).collect(Collectors.toList());
	}

	public List<Report> getOpenReports() {
		return reports.stream().filter(Report::isOpen).collect(Collectors.toList());
	}

	public List<Report> getReports() {
		return reports;
	}

	public List<String> getLogs(UUID uuid) {
		return chatListener.getLogs(uuid);
	}

	public void addReport(Report report) {
		reports.add(report);

		ScorchCore.getInstance().getDataManager().saveObjectAsync("reports", report);
	}

	public void updateReport(Report report) {
		ScorchCore.getInstance().getDataManager().updateObjectAsync("reports", report,
				new SQLSelector("id", report.getId()));
	}

	public Inventory getReportGUI(String title) {
		Inventory inv = Bukkit.createInventory(null, 27, title);

		inv.setItem(10, ReportType.GAMEPLAY.getItem());
		inv.setItem(13, ReportType.CLIENT.getItem());
		inv.setItem(16, ReportType.CHAT.getItem());

		ItemStack bg = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		ItemMeta meta = bg.getItemMeta();
		meta.setDisplayName(MSG.color("&r"));
		bg.setItemMeta(meta);

		for (int i = 0; i < inv.getSize(); i++) {
			if (inv.getItem(i) != null)
				continue;
			inv.setItem(i, bg);
		}

		return inv;
	}

	public Inventory getResolutionGUI() {
		Inventory inv = Bukkit.createInventory(null, 27, "Resolving report...");

		inv.setItem(10, ResolutionType.CONFIRMED.getItem());
		inv.setItem(12, ResolutionType.REJECTED.getItem());
		inv.setItem(14, ResolutionType.ABUSE.getItem());
		inv.setItem(16, ResolutionType.CANCEL.getItem());

		ItemStack bg = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta meta = bg.getItemMeta();
		meta.setDisplayName(MSG.color("&r"));
		bg.setItemMeta(meta);

		for (int i = 0; i < inv.getSize(); i++) {
			if (inv.getItem(i) != null)
				continue;
			inv.setItem(i, bg);
		}

		return inv;
	}

	public Report getReport(String id) {
		return reports.stream().filter(r -> r.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
	}

	public BukkitRunnable sendReportMessages() {
		BukkitRunnable runner = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					sendReportInfo(p);
				}
			}
		};

		return runner;
	}

	public void sendReportInfo(Player p) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(p.getUniqueId());
		if (!sp.hasData("assignedreport"))
			return;

		Report report = getReport(sp.getData("assignedreport", String.class));

		if (report == null)
			return;
		MSG.tell(p, " ");
		MSG.tell(p, "&cYou are currently assigned to a report!");
		MSG.tell(p, " ");
		MSG.tell(p,
				"&7Report ID: &8" + report.getId() + " &7[Submitted &8" + sdf.format(report.getReportDate()) + "&7]");
		MSG.tell(p, "&3Reporter: &b" + Bukkit.getOfflinePlayer(report.getReporter()).getName());
		MSG.tell(p, "&3Reported: &c" + Bukkit.getOfflinePlayer(report.getTarget()).getName());
		MSG.tell(p, "&7Reason: &e" + report.getReason() + " [" + report.getType() + "]");
		MSG.tell(p, " ");
		if (report.getServer() != null) {
			MSG.tell(p, "&7Server: &a" + report.getServer());
			MSG.tell(p, " ");
		}

		TextComponent cmp = new TextComponent(MSG.color("&d&lChat Logs: &5"));
		cmp.setExtra(
				Arrays.asList(new ComponentBuilder("HERE").event(new ClickEvent(Action.OPEN_URL, report.getPastebin()))
						.event(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder(MSG.color("&7Open Logs")).create()))
						.create()));
		if (report.getType() == ReportType.CHAT) {
			if (report.getPastebin() == null) {
				MSG.tell(p, "&cChat Logs are Unavailable");
			} else {
				p.spigot().sendMessage(cmp);
			}
		}
	}

	public void sendReportInfo(CommandSender p, Report report) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

		MSG.tell(p, " ");
		MSG.tell(p, "&cReport #" + report.getId() + " Information");
		MSG.tell(p, " ");
		MSG.tell(p,
				"&7Report ID: &8" + report.getId() + " &7[Submitted &8" + sdf.format(report.getReportDate()) + "&7]");
		MSG.tell(p, "&3Reporter: &b" + Bukkit.getOfflinePlayer(report.getReporter()).getName());
		MSG.tell(p, "&3Reported: &c" + Bukkit.getOfflinePlayer(report.getTarget()).getName());
		MSG.tell(p, "&7Reason: &e" + report.getReason() + " [" + report.getType() + "]");
		MSG.tell(p, " ");
		if (report.getServer() != null) {
			MSG.tell(p, "&7Server: &a" + report.getServer());
			MSG.tell(p, " ");
		}

		if (report.isHandled()) {
			MSG.tell(p, "&6Staff: &e" + report.getStaff());
			MSG.tell(p, "&6Resolution: &e" + report.getResolution() + " &7[&e"
					+ MSG.camelCase(report.getResolutionType() + "") + "&7]");
			MSG.tell(p, "&6Resolved On: " + sdf.format(report.getHandledDate()));
		}

		TextComponent cmp = new TextComponent(MSG.color("&d&lChat Logs: &5"));
		cmp.setExtra(
				Arrays.asList(new ComponentBuilder("HERE").event(new ClickEvent(Action.OPEN_URL, report.getPastebin()))
						.event(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder(MSG.color("&7Open Logs")).create()))
						.create()));
		if (report.getType() == ReportType.CHAT) {
			if (report.getPastebin() == null) {
				MSG.tell(p, "&cChat Logs are Unavailable");
			} else {
				p.spigot().sendMessage(cmp);
			}
		}
	}

}
