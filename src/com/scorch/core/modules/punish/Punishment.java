package com.scorch.core.modules.punish;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.scorch.core.utils.MSG;

public class Punishment {
	private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a");

	private UUID target;
	private String staff, reason, unbanner, unbanreason;

	private long date, duration, unbandate;

	private PunishType punishType;

	public Punishment(UUID target, String staff, String reason, long date, long duration, PunishType type) {
		this.target = target;

		this.staff = staff;
		this.reason = reason;

		this.date = date;
		this.duration = duration;
		this.punishType = type;
	}

	public Punishment(UUID target, String staff, String reason, long date, long duration, PunishType type,
			String unbanner, String unbanreason, long unbandate) {
		this(target, staff, unbanreason, unbandate, duration, type);

		this.unbanner = unbanner;
		this.unbanreason = unbanreason;
		this.unbandate = unbandate;
	}
	
	public UUID getTargetUUID() {
		return target;
	}

	public OfflinePlayer getTargetPlayer() {
		return Bukkit.getOfflinePlayer(target);
	}

	public String getStaffName() {
		return staff;
	}

	public String getReason() {
		return reason;
	}

	public String getUnbanReason() {
		return unbanreason;
	}

	public PunishType getType() {
		return punishType;
	}

	public long getUnbanDate() {
		return unbandate;
	}

	public ItemStack getItem() {
		ItemStack item = new ItemStack(punishType.getMaterial());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(MSG.color("&r" + punishType.toString()));
		List<String> lore = new ArrayList<>();
		lore.add(MSG.color("&ePunisher: " + staff));
		lore.add(MSG.color("&eReason: " + reason));
		lore.add(MSG.color("&eDate: " + sdf.format(date)));
		if (punishType != PunishType.WARNING && punishType != PunishType.KICK)
			lore.add(MSG.color("&eDuration: " + MSG.getTime(duration)));

		if (isRemoved()) {
			lore.add(MSG.color("&eRemoved By: " + unbanner));
			lore.add(MSG.color("&eRemove Reason: " + unbanreason));
			lore.add(MSG.color("&eRemove Date: " + sdf.format(unbandate)));
		}

		return item;
	}

	public boolean isActive() {
		return isRemoved() || date + duration >= System.currentTimeMillis();
	}

	public boolean isRemoved() {
		return unbanner != null;
	}
}
