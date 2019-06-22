package com.scorch.core.modules.punish;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class Punishment {
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

	public boolean isActive() {
		return isRemoved() || date + duration >= System.currentTimeMillis();
	}

	public boolean isRemoved() {
		return unbanner != null;
	}
}
