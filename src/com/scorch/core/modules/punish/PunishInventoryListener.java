package com.scorch.core.modules.punish;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.CPlayer;
import com.scorch.core.utils.MSG;

public class PunishInventoryListener implements Listener {
	public PunishInventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@SuppressWarnings({ "unused" })
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		CPlayer cp = ScorchCore.getInstance().getDataManager().getPlayer(player);
		ItemStack hand = event.getCurrentItem();
		PunishType type = PunishType.OTHER;
		if (hand == null || hand.getType() == Material.AIR)
			return;
		if (cp.getTempData("openInventory") != null && cp.getTempString("punishing") != null) {
			event.setCancelled(true);
			int page = (int) cp.getTempInteger("page");
			OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(cp.getTempString("punishing")));
			String reason = cp.getTempString("reason");
			if (event.getClick() == ClickType.RIGHT
					|| (event.getClick() == ClickType.SHIFT_LEFT && player.hasPermission("scorch.punish.delete"))) {
				if (reason != null && hand.getType() != Material.ARROW) {
					List<Punishment> history = ScorchCore.getInstance().getPunishModule()
							.getPunishments(target.getUniqueId());
					if (history == null) {
						history = new ArrayList<Punishment>();
					}
					int idd = history.size() - 1 - (page * (event.getInventory().getSize() - 9) + event.getRawSlot());

					Punishment p = history.get(idd);

					if (event.getClick() == ClickType.SHIFT_LEFT) {
						// Delete punishment from history
						// Make sure to handle IP bans
					} else {
						if (!(p.getDuration() == -1
								|| p.getDate() + p.getDuration() > (double) System.currentTimeMillis()))
							return;
						p.remove(player.getName(), reason);
						// handle IP bans
					}
				}
			}
			if (event.getRawSlot() == event.getInventory().getSize() - 1) {
				page++;
			}
			if (event.getRawSlot() == event.getInventory().getSize() - 9) {
				page--;
			}

			player.openInventory(ScorchCore.getInstance().getPunishModule().getPunishGUI(target));
			cp.setTempData("punishing", target.getUniqueId() + "");
			cp.setTempData("page", page);
			cp.setTempData("reason", reason);
		}

		if (cp.getTempData("openInventory") == null)
			return;
		event.setCancelled(true);
		OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(cp.getTempString("punishing")));
		List<Punishment> history = ScorchCore.getInstance().getPunishModule().getPunishments(target.getUniqueId());

		if (history == null) {
			history = new ArrayList<>();
		}
		String reason = cp.getTempString("reason");
		if (event.getClick() == ClickType.RIGHT
				|| (event.getClick() == ClickType.SHIFT_LEFT && player.hasPermission("rank.dev"))) {
			double id = (event.getRawSlot() - 8) / 9;
			if (id != Math.round(id))
				return;

			Punishment p = history.get(history.size() - 1 - (int) Math.round(id));
			if (event.getClick() == ClickType.SHIFT_LEFT) {
				// Delete punishment
			} else {
				if (!(p.getDuration() == -1 || p.getDuration() + p.getDuration() > (double) System.currentTimeMillis()))
					return;
				// Remove punishment
			}

			player.closeInventory();
			return;
		}
		if (event.getRawSlot() == event.getInventory().getSize() - 1) {
			player.openInventory(ScorchCore.getInstance().getPunishModule().getHistoryGUI(target));
		}
		String id = "";
		for (String res : ScorchCore.getInstance().getGui().getConfigurationSection("punish").getKeys(false)) {
			if (ScorchCore.getInstance().getGui().getInt("punish." + res + ".Slot") == event.getRawSlot()) {
				id = res;
				break;
			}
		}

		if (ScorchCore.getInstance().getGui().contains("punish." + id + ".SendToTarget") && target.isOnline()) {
			ScorchCore.getInstance().getGui().getStringList("punish." + id + ".SendToTarget").forEach((line) -> {
				MSG.tell(((Player) target), line.replace("%player%", player.getName()).replace("%reason%", reason));
			});
			player.closeInventory();
		}
		if (ScorchCore.getInstance().getGui().contains("punish." + id + ".SendToStaff")) {
			ScorchCore.getInstance().getGui().getStringList("punish." + id + ".SendToStaff").forEach((line) -> {
				Bukkit.getOnlinePlayers().forEach((p) -> {
					if (p.hasPermission("rank.staff")) {
						MSG.tell(p, line.replace("%player%", player.getName()).replace("%reason%", reason)
								.replace("%target%", target.getName()));
					}
				});
			});
			player.closeInventory();
		}

		if (id.contains("kick")) {
			if (target.isOnline()) {
				((Player) target).kickPlayer(MSG.color("you will be kicked"));
			}
			type = PunishType.KICK;
		}

		if (id.contains("warning")) {
			if (target.isOnline())
				((Player) target).playSound(((Player) target).getLocation(), Sound.ENTITY_CAT_PURREOW, 1, 1);
			type = PunishType.WARNING;
		}

		// TODOD
		if (id.contains("ip")) {
			String ip = null;
			if (ip == null) {
				MSG.tell(player, "&cPunish &4>> &7That player does not have a logged IP.");
				return;
			}
			for (Player t : Bukkit.getOnlinePlayers()) {
				if (t.getAddress().getHostName().equals(ip)) {
					t.kickPlayer(MSG
							.color("&c&lYou have been Permanently IP-Banned by " + player.getName() + "\n&r" + reason));
				}
			}
//			Main.plugin.data.set("IPBans." + ip.replace(".", ",") + ".reason", reason);
//			Main.plugin.data.set("IPBans." + ip.replace(".", ",") + ".banner", player.getName());
			type = PunishType.IP_BAN;
		}

		if (!(ScorchCore.getInstance().getGui().contains("punish." + id + ".MuteTime")
				|| ScorchCore.getInstance().getGui().contains("punish." + id + ".BanTime") || id.contains("warning")
				|| id.contains("kick") || id.contains("ip"))) {
			return;
		}

		player.closeInventory();

		double dur = 0;
		if (ScorchCore.getInstance().getGui().contains("punish." + id + ".MuteTime")) {
			dur = ScorchCore.getInstance().getGui().getDouble("punish." + id + ".MuteTime");
			type = PunishType.PERM_MUTE;
		}
		if (ScorchCore.getInstance().getGui().contains("punish." + id + ".BanTime")) {
			dur = ScorchCore.getInstance().getGui().getDouble("punish." + id + ".BanTime");
			if (target.isOnline())
				if (dur == -1) {
					String msg = "perm ban";
					((Player) target).kickPlayer(MSG.color(msg));
					type = PunishType.PERM_BAN;
				} else {
					String msg = "temp ban";
					((Player) target).kickPlayer(MSG.color(msg));
					type = PunishType.TEMP_BAN;
				}
		}

		Punishment punishment = new Punishment(target.getUniqueId(), player.getName(), cp.getTempString("reason"),
				System.currentTimeMillis(), (long) dur, type);

		ScorchCore.getInstance().getPunishModule().addPunishment(punishment);
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		for (String id : new String[] { "punishing", "reason", "page" })
			cp.removeTempData(id);

	}

}
