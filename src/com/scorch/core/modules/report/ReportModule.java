package com.scorch.core.modules.report;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.modules.report.Report.ReportType;
import com.scorch.core.utils.MSG;

public class ReportModule extends AbstractModule {

	private List<Report> reports;

	private Listener listener;

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
	}

	public List<Report> getReports(UUID player) {
		return reports.parallelStream().filter(r -> r.getReporter().equals(player)).collect(Collectors.toList());
	}

	public List<Report> getReportsAgainst(UUID player) {
		return reports.parallelStream().filter(r -> r.getTarget().equals(player)).collect(Collectors.toList());
	}

	public void addReport(Report report) {
		reports.add(report);

		ScorchCore.getInstance().getDataManager().saveObjectAsync("reports", report);
	}

	public void updateReport(Report report) {
		ScorchCore.getInstance().getDataManager().updateObjectAsync("reports", report,
				new SQLSelector("id", report.getId()));
	}

	public Inventory getReportGUI(UUID target) {
		String name = Bukkit.getOfflinePlayer(target).getName();
		if (name == null)
			name = target.toString();

		Inventory inv = Bukkit.createInventory(null, 27, "Reporting " + name + "...");

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

	@Override
	public void disable() {
		InventoryClickEvent.getHandlerList().unregister(listener);
		InventoryCloseEvent.getHandlerList().unregister(listener);
		reports.clear();
	}

}
