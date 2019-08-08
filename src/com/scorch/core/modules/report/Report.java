package com.scorch.core.modules.report;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.annotations.DataNotNull;
import com.scorch.core.modules.data.annotations.DataPrimaryKey;
import com.scorch.core.modules.staff.TrustModule;
import com.scorch.core.utils.MSG;

public class Report implements Comparable<Report> {
	@DataNotNull @DataPrimaryKey
	private String id;

	@DataNotNull
	private UUID reporter, target;
	@DataNotNull
	private ReportType type;
	@DataNotNull
	private String reason;

	private String pastebin, staff, server, resolution;
	private long reportDate, handleDate;
	private ResolutionType resType;

	public Report() {

	}

	public Report(UUID reporter, UUID target, ReportType type, String reason) {

		ReportModule rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);
		do {
			this.id = MSG.genUUID(6);
		} while (rm.getReport(id) != null);

		this.reporter = reporter;
		this.target = target;
		this.type = type;
		this.reason = reason;
		this.reportDate = System.currentTimeMillis();
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void resolve(String staff, ResolutionType type, String resolution) {
		this.handleDate = System.currentTimeMillis();
		this.staff = staff;
		this.resolution = resolution;
		this.resType = type;
	}

	public boolean isHandled() {
		return staff != null;
	}

	public boolean isOpen() {
		return staff == null;
	}

	public void setPastebin(String pastebin) {
		this.pastebin = pastebin;
	}

	public String getPastebin() {
		return pastebin;
	}

	public String getStaff() {
		return staff;
	}

	public String getServer() {
		return server;
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

	public ResolutionType getResolutionType() {
		return resType;
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

	public enum ResolutionType {
		CONFIRMED("CACTUS_GREEN", "&a&lConfirm", "&7Confirm that the report was accurate", "&7and punish the suspect",
				"", "&cWill Open Punish GUI"),
		REJECTED("ROSE_RED", "&c&lReject", "&7Reject the report due to", "&7insufficient evidence, no rules broken,",
				"&7reported too late, etc."),
		CANCEL("BARRIER", "&e&lCancel", "&7Cancel handling of the report and let", "&7another staff member handle it."),
		ABUSE("BEDROCK", "&6&lAbuse", "&7Mark the report as abuse of ", "&7the report system and deny it.");
		private Material mat;
		private List<String> desc;

		private ItemStack stack;

		ResolutionType(String material, String name, String... desc) {
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

	@Override
	public int compareTo(Report o) {
		if (isOpen() == o.isOpen()) {
			OfflinePlayer off = Bukkit.getOfflinePlayer(target), oOff = Bukkit.getOfflinePlayer(o.getTarget());

			if (off.isOnline() != oOff.isOnline() && (type != ReportType.CHAT && o.getType() != ReportType.CHAT)) {
				return off.isOnline() ? 1 : -1;
			}

			ReportModule rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);

			List<Report> reports = rm.getReports(target), oReports = rm.getReports(o.getTarget());

			int recent = reports.stream().filter(r -> System.currentTimeMillis() - r.getReportDate() < 2.628e+9)
					.collect(Collectors.toList()).size(),
					oRecent = oReports.stream().filter(r -> System.currentTimeMillis() - r.getReportDate() < 2.628e+9)
							.collect(Collectors.toList()).size();

			if (recent != oRecent) {
				return recent > oRecent ? 1 : -1;
			}

			TrustModule tm = ScorchCore.getInstance().getModule("TrustModule", TrustModule.class);
			double trust = tm.getTrust(target), oTrust = tm.getTrust(o.getTarget());

			if (trust != oTrust)
				return trust > oTrust ? 1 : -1;

			return 0;
		} else if (isOpen() && !o.isOpen()) {
			return 1;
		} else {
			return -1;
		}
	}
}
