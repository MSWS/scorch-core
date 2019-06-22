package com.scorch.core.modules.punish;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.scorch.utils.MSG;

public class Punishment {
	private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a");

	private UUID target;
	private String staff, reason, remover, removeReason;

	private long date, duration, removeDate;

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
			String remover, String removeReason, long removeDate) {
		this(target, staff, removeReason, removeDate, duration, type);

		this.remover = remover;
		this.removeReason = removeReason;
		this.removeDate = removeDate;
	}

	public void remove(String remover, String removeReason) {
		this.remover = remover;
		this.removeReason = removeReason;
		this.removeDate = System.currentTimeMillis();
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

	public String getRemoveReason() {
		return removeReason;
	}

	public PunishType getType() {
		return punishType;
	}

	public long getRemoveDate() {
		return removeDate;
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
			lore.add(MSG.color("&eRemoved By: " + remover));
			lore.add(MSG.color("&eRemove Reason: " + removeReason));
			lore.add(MSG.color("&eRemove Date: " + sdf.format(removeDate)));
		}

		return item;
	}

	public boolean isActive() {
		return isRemoved() || date + duration >= System.currentTimeMillis();
	}

	public boolean isRemoved() {
		return remover != null;
	}
}
