package com.scorch.core.modules.punish;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.MSG;

/**
 * Punishment is an entry to a player's Punishment History, supports
 * OfflinePlayer banning, run the execute function to run commands/punishments
 * 
 * @author imodm
 *
 */
public class Punishment {

	@DataIgnore
	final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a");

	private UUID target;
	private String staff, reason, remover, removeReason;

	private long date, duration, removeDate;

	private PunishType punishType;

	/**
	 * Creates a new punishment
	 * 
	 * @param target   UUID of the player to be punished
	 * @param staff    Staff Username of the {@link CommandSender} that applied the
	 *                 punishment
	 * @param reason   Reason for the punishment
	 * @param date     Date of creation
	 * @param duration How long the punishment is (-1 for permanent)
	 * @param type     {@link PunishType} type of punishment
	 */
	public Punishment(UUID target, String staff, String reason, long date, long duration, PunishType type) {
		this.target = target;

		this.staff = staff;
		this.reason = reason;

		this.date = date;
		this.duration = duration;
		this.punishType = type;
	}

	/**
	 * Creates a new punishment with a remover
	 * 
	 * @see Punishment
	 */
	public Punishment(UUID target, String staff, String reason, long date, long duration, PunishType type,
			String remover, String removeReason, long removeDate) {
		this(target, staff, removeReason, removeDate, duration, type);

		this.remover = remover;
		this.removeReason = removeReason;
		this.removeDate = removeDate;
	}

	/**
	 * Empty contructor for the datamanager
	 */
	public Punishment() {
	}

	/**
	 * Runs the punishment and any appropriate actions. (Sending messages to staff
	 * members, kicking players, etc.) Ideally should only be run once.
	 */
	public void execute() {
		Player target = Bukkit.getPlayer(this.target);
		kick: if (punishType.restrictsLogin()) {
			if (target == null || !target.isOnline())
				break kick;

			target.kickPlayer(null);
		}
	}

	/**
	 * Renders the punishment ineffective <b>does not delete the punishment</b> uses
	 * the {@link System#currentTimeMillis()} as removal date
	 * 
	 * @param remover      Staff Username of the {@link CommandSender} that applied
	 *                     the punishment.
	 * @param removeReason Reason that the punishment was removed.
	 */
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
	
	public long getDuration() {
		return duration;
	}

	public ItemStack getItem() {
		ItemStack item = new ItemStack(punishType.getMaterial());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(MSG.color("&r" + punishType.getColored()));
		List<String> lore = new ArrayList<>();
		lore.add(MSG.color("&ePunisher: " + staff));
		lore.add(MSG.color("&eReason: " + reason));
		lore.add(MSG.color("&eDate: " + sdf.format(date)));
		if (punishType != PunishType.WARNING && punishType != PunishType.KICK)
			lore.add(MSG.color("&eDuration: " + (duration == -1 ? "Permanent" : MSG.getTime(duration))));

		if (isRemoved()) {
			lore.add(MSG.color("&eRemoved By: " + remover));
			lore.add(MSG.color("&eRemove Reason: " + removeReason));
			lore.add(MSG.color("&eRemove Date: " + sdf.format(removeDate)));
		}

		meta.setLore(lore);

		if (isActive()) {
			meta.addEnchant(Enchantment.DURABILITY, 0, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		}

		item.setItemMeta(meta);
		return item;
	}

	public boolean isActive() {
		return isRemoved() || date + duration >= System.currentTimeMillis();
	}

	public boolean isRemoved() {
		return remover != null;
	}

	/**
	 * Gets the kick message that is formatted for when the player tries to login
	 * while punished with a {@link PunishType#restrictsLogin()} true punishment
	 * 
	 * Will return null if no kick message is appropriate.
	 * 
	 * @return Single line message with \n and '&' that should be appropriately
	 *         formatted (\n) is automatically by the MC client
	 */
	public String getKickMessage() {
		if (!punishType.restrictsLogin()) {
			Logger.warn("Attempted to get kick message of a non-kickable punishment type. (Type: " + punishType + ")");
			return null;
		}

		return "&c&lYou have been " + getVerb() + " by " + staff + " for " + duration + " (Reason: " + reason + ")";
	}

	public String getVerb() {
		switch (punishType) {
		case IP_BAN:
			return "ip banned";
		case KICK:
			return "kicked";
		case OTHER:
			return "punished";
		case PERM_BAN:
			return "permanently banned";
		case PERM_MUTE:
			return "permanently muted";
		case TEMP_BAN:
			return "temporarily banned";
		case TEMP_MUTE:
			return "temporarily muted";
		case WARNING:
			return "warned";
		default:
			Logger.warn("Unknown punish type: " + punishType);
			return "punished";
		}
	}
}
