package com.scorch.core.modules.punish;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.scorch.core.ScorchCore;
import com.scorch.core.events.punishment.PunishmentCreateEvent;
import com.scorch.core.events.punishment.PunishmentEvent;
import com.scorch.core.events.punishment.PunishmentUpdateEvent;
import com.scorch.core.events.punishment.TestEvent;
import com.scorch.core.modules.communication.CommunicationModule;
import com.scorch.core.modules.communication.exceptions.WebSocketException;
import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.modules.data.annotations.DataPrimaryKey;
import com.scorch.core.modules.data.exceptions.DataUpdateException;
import com.scorch.core.modules.messages.CMessage;
import com.scorch.core.modules.messages.MessagesModule;
import com.scorch.core.modules.messages.OfflineMessage;
import com.scorch.core.modules.messages.OfflineMessagesModule;
import com.scorch.core.modules.players.IPTracker;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.MSG;

/**
 * Punishment is an entry to a player's Punishment History, supports
 * OfflinePlayer banning, run the execute function to run commands/punishments
 * (should only be run once)
 * 
 * @author imodm
 *
 */
public class Punishment implements Comparable<Punishment> {

	@DataIgnore
	private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a");

	@DataPrimaryKey
	private UUID id;
	private UUID target;
	private String staff, reason, remover, removeReason, ip, info;

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
		this.id = UUID.randomUUID();
		this.target = target;

		this.staff = staff;
		this.reason = reason;

		this.date = date;
		this.duration = duration;
		this.punishType = type;
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
		CommunicationModule cm = ScorchCore.getInstance().getCommunicationModule();
		try {
			cm.dispatchEvent(new PunishmentCreateEvent(this));
			cm.dispatchEvent(new TestEvent(this));
		} catch (WebSocketException e) {
			e.printStackTrace();
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(this.target);
		ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(target.getUniqueId());
		IPTracker it = (IPTracker) ScorchCore.getInstance().getModule("IPTrackerModule");

		kick: if (punishType.restrictsLogin()) {
			if (!target.isOnline())
				break kick;

			target.getPlayer().kickPlayer(getKickMessage());
		}

		if (punishType == PunishType.IP_BAN) {
			String ip = sp.getData("lastip", String.class);
			if (ip == null)
				return;
			this.ip = ip;

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getAddress().getHostName().equals(ip))
					p.kickPlayer(getKickMessage());
			}
		}

		if (punishType == PunishType.BLACKLIST) {
			if (it == null) {
				Logger.error("IP Tracker Module is NULL");
			} else {
				for (UUID p : it.linkedAccounts(this.target).stream()
						.filter(uuid -> Bukkit.getOfflinePlayer(uuid).isOnline()).collect(Collectors.toList())) {
					Bukkit.getPlayer(p).kickPlayer(getKickMessage());
				}
			}

		}

		if (punishType == PunishType.WARNING) {
			String warning = ScorchCore.getInstance().getMessage("warningmessage").replace("%staff%", staff)
					.replace("%reason%", reason);
			for (String msg : warning.split("\\|"))
				if (target.isOnline()) {
					MSG.tell(target.getPlayer(), msg);
				} else {
					OfflineMessagesModule omm = (OfflineMessagesModule) ScorchCore.getInstance()
							.getModule("OfflineMessagesModule");
					omm.addMessage(new OfflineMessage("> ", target.getUniqueId(), msg));
				}
		}

		MSG.tell("scorch.punish.notify",
				ScorchCore.getInstance().getMessage("punishmessage").replace("%staff%", staff)
						.replace("%target%", target.getName()).replace("%reason%", reason)
						.replace("%duration%", MSG.getTime(duration)).replace("%verb%", getVerb()));
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

		update();
	}

	public void update() {
		try {
			ScorchCore.getInstance().getDataManager().updateObject("punishments", this);
		} catch (DataUpdateException e) {
			e.printStackTrace();
		}

		PunishmentEvent pe = new PunishmentUpdateEvent(this);
		try {
			ScorchCore.getInstance().getCommunicationModule().dispatchEvent(pe);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}

	public UUID getId() {
		return id;
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

	public String getIP() {
		return ip;
	}

	public String getInfo() {
		return info;
	}

	public boolean hasInfo() {
		return info != null;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public ItemStack getItem() {
		ItemStack item = new ItemStack(punishType.getMaterial());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(MSG.color("&r" + punishType.getColored()));
		List<String> lore = new ArrayList<>();
		lore.add(MSG.color("&3Staff: &b" + staff));
		lore.add(MSG.color("&3Reason: &b" + reason));
		if (punishType == PunishType.IP_BAN)
			lore.add(MSG.color("&6IP: &e" + ip));
		lore.add("");
		lore.add(MSG.color("&2Date: &a" + sdf.format(date)));
		if (punishType != PunishType.WARNING) {
			lore.add(MSG.color("&2Duration: &a" + (duration == -1 ? "&cPermanent" : MSG.getTime(duration))));
			if (duration != -1 && isActive())
				lore.add(MSG.color("&2Time Left: &a" + MSG.getTime((date + duration - System.currentTimeMillis()))));
		}

		if (isRemoved()) {
			lore.add("");
			lore.add(MSG.color("&cRemoved By: &4" + remover));
			lore.add(MSG.color("&cRemove Reason: &4" + removeReason));
			lore.add(MSG.color("&cRemove Date: &4" + sdf.format(removeDate)));
		}

		meta.setLore(lore);

		if (isActive()) {
			meta.addEnchant(Enchantment.DURABILITY, 0, true);
		}

		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);

		item.setItemMeta(meta);
		return item;
	}

	public boolean isActive() {
		return !isRemoved() && (date + duration >= System.currentTimeMillis() || duration == -1);
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

		MessagesModule messages = ScorchCore.getInstance().getMessages();

		Map<String, String> place = new HashMap<>();
		place.put("%verb%", getVerb());
		place.put("%staff%", staff);
		place.put("%duration%", MSG.getTime(duration));
		place.put("%reason%", reason);
		place.put("%timeleft%", MSG.getTime(date + duration - System.currentTimeMillis()));
		place.put("%appeal%", messages.getMessage("appeallink").getMessage());

		CMessage msg = duration == -1 ? messages.getMessage("permbanmessage") : messages.getMessage("tempbanmessage");

		msg.applyPlaceholders(place);
		return msg.getMessage();
	}

	public String getVerb() {
		switch (punishType) {
		case IP_BAN:
			return "ip banned";
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
		case BLACKLIST:
			return "blacklisted";
		case REPORT_BAN:
			return "report banned";
		default:
			Logger.warn("Unknown punish type: " + punishType);
			return "punished";
		}
	}

	@Override
	public int compareTo(Punishment o) {
		return o.getDate() > date ? 1 : -1;
	}
}
