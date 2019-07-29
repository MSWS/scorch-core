package com.scorch.core.modules.report;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.scorch.core.modules.data.annotations.DataNotNull;
import com.scorch.core.utils.MSG;

public class Report {
	@DataNotNull
	private String id;

	@DataNotNull
	private UUID reporter, target;
	@DataNotNull
	private ReportType type;
	@DataNotNull
	private String reason;

	private String staff, resolution;
	private long reportDate, handleDate;

	public Report() {

	}

	public Report(UUID reporter, UUID target, ReportType type, String reason) {
		this.id = MSG.genUUID(6);

		this.reporter = reporter;
		this.target = target;
		this.type = type;
		this.reason = reason;
		this.reportDate = System.currentTimeMillis();
	}

	public void handle(String staff, String resolution) {
		this.handleDate = System.currentTimeMillis();
		this.staff = staff;
		this.resolution = resolution;
	}

	public boolean isHandled() {
		return staff != null;
	}

	public boolean isOpen() {
		return staff == null;
	}

	public String getStaff() {
		return staff;
	}

	public long getReportDate() {
		return reportDate;
	}

	public long getHandledDate() {
		return handleDate;
	}

	public String getResolution() {
		return resolution;
	}

	public UUID getReporter() {
		return reporter;
	}

	public UUID getTarget() {
		return target;
	}

	public ReportType getType() {
		return type;
	}

	public String getReason() {
		return reason;
	}

	public String getId() {
		return id;
	}

	public enum ReportType {
		CHAT("PAPER", "&a&lChat", "&7Filter Bypass, Toxicity, Harrassment, etc."),
		GAMEPLAY("GRASS_BLOCK", "&e&lGameplay", "&7Breaking game rules, camping, ghosting, etc."),
		CLIENT("DIAMOND_SWORD", "&c&lClient", "&7Hacking, X-Ray, Cheating");

		private Material mat;
		private List<String> desc;

		private ItemStack stack;

		ReportType(String material, String name, String... desc) {
			this.mat = Material.valueOf(material);
			this.desc = new ArrayList<String>();
			for (String s : desc)
				this.desc.add(MSG.color(s));

			stack = new ItemStack(mat);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(MSG.color(name));
			meta.setLore(this.desc);
			stack.setItemMeta(meta);
		}

		public ItemStack getItem() {
			return stack;
		}

	}
}
