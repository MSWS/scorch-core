package com.scorch.core.modules.punish;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.scorch.utils.MSG;

public class Punishment implements Comparable<Punishment>, ConfigurationSerializable {
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

	public void execute() {
		Player target = Bukkit.getPlayer(this.target);
		kick: if (punishType.restrictsLogin()) {
			if (target == null || !target.isOnline())
				break kick;

			target.kickPlayer(null);
		}
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

	public long getDate() {
		return date;
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

	/**
	 * @deprecated
	 */
	@Override
	public int compareTo(Punishment o) {
		return getDate() > o.getDate() ? -1 : 1;
	}

	@Override
	public Map<String, Object> serialize() {
		return null;
	}

	public static Punishment deserialize(Map<String, Object> values) {
		if (values.containsKey("remover")) {
			return new Punishment(UUID.fromString((String) values.get("target")), (String) values.get("staff"),
					(String) values.get("reason"), (long) values.get("date"), (long) values.get("duration"),
					PunishType.valueOf((String) values.get("type")), (String) values.get("remover"),
					(String) values.get("removeReason"), (long) values.get("removeDate"));
		}
		return new Punishment(UUID.fromString((String) values.get("target")), (String) values.get("staff"),
				(String) values.get("reason"), (long) values.get("date"), (long) values.get("duration"),
				PunishType.valueOf((String) values.get("type")));
	}

	public String getKickMessage() {
		if (!punishType.restrictsLogin()) {
			return "Unknown";
		}

		String verb = "punished"; // should never appear
		switch (punishType) {
		case IP_BAN:
			verb = "ip banned";
			break;
		}

		// TODO

		return "&c&lYou have been ";
	}
}
