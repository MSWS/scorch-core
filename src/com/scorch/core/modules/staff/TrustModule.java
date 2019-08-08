package com.scorch.core.modules.staff;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.scorch.core.utils.CItem;
import com.scorch.core.utils.MSG;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.players.FriendModule;
import com.scorch.core.modules.players.Friendship.FriendStatus;
import com.scorch.core.modules.players.IPTracker;
import com.scorch.core.modules.players.PlaytimeModule;
import com.scorch.core.modules.punish.Punishment;
import com.scorch.core.modules.report.ReportModule;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TrustModule extends AbstractModule {

	public TrustModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {

	}

	@Override
	public void disable() {

	}

	/**
	 * TODO
	 * 
	 * @param uuid
	 * @return
	 */
	@SuppressWarnings("unused")
	public double getTrust(UUID uuid) {
		long start = System.currentTimeMillis();
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		if (!player.hasPlayedBefore())
			return .5;

		String lastIp = ScorchCore.getInstance().getDataManager().getScorchPlayer(uuid).getData("lastIp", String.class,
				"192.0.0.1");

		List<Punishment> history = ScorchCore.getInstance().getPunishModule().getPunishments(uuid);

		int punishments = history.size();
		int friends = ScorchCore.getInstance().getModule("FriendModule", FriendModule.class).getFriends(uuid)
				.parallelStream().filter(f -> f.getStatus() == FriendStatus.FRIENDS).collect(Collectors.toList())
				.size();
		int directAlts = ScorchCore.getInstance().getModule("IPTrackerModule", IPTracker.class)
				.getAccountsWithIP(lastIp).size();
		int otherAlts = ScorchCore.getInstance().getModule("IPTrackerModule", IPTracker.class).linkedAccounts(uuid)
				.size();

		int gamesPlayed = 0;

		ReportModule rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);

		int reportsAgainst = rm.getReportsAgainst(uuid).size();
		int reportsSubmitted = rm.getReports(uuid).size();

		long playtime = ScorchCore.getInstance().getModule("PlaytimeModule", PlaytimeModule.class).getPlaytime(uuid);

		double seconds = playtime / 1000.0, minutes = seconds / 60.0, hours = minutes / 60.0;

		double punishmentScore = 0;

		for (Punishment p : history) {
			switch (p.getType()) {
			case BLACKLIST:
				punishmentScore += 100.0;
				break;
			case IP_BAN:
				punishmentScore += 90.0;
				break;
			case REPORT_BAN:
				punishmentScore += 40.0;
				break;
			case OTHER:
				punishmentScore += 20.0;
				break;
			case PERM_BAN:
				punishmentScore += 80.0;
				break;
			case PERM_MUTE:
				punishmentScore += 50.0;
				break;
			case TEMP_BAN:
				punishmentScore += Math.min((p.getDuration() / 1000.0 / 60.0 / 60.0) * 5.0, 100.0);
				break;
			case TEMP_MUTE:
				punishmentScore += Math.min((p.getDuration() / 1000.0 / 60.0) * 45.0, 100.0);
				break;
			case WARNING:
				punishmentScore += 10.0;
				break;
			}
		}

		double proV = Math.max(minutes + gamesPlayed * 5 + friends * 2.5, 1);
		double punishV = Math.max(punishmentScore + directAlts * 20 + otherAlts * 10, 1);
		double reportV = Math.max(reportsAgainst * 20 + reportsSubmitted * 10, 1);

		return (proV / (punishV + reportV));
	}

	public Inventory getInventory(UUID uuid) {
		Inventory inv = Bukkit.createInventory(null, 36, "Trust Level Description");
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		ItemStack glass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		ItemMeta meta = glass.getItemMeta();
		meta = glass.getItemMeta();
		meta.setDisplayName(MSG.color("&r"));
		glass.setItemMeta(meta);
		if (!player.hasPlayedBefore()) {
			ItemStack barrier = new ItemStack(Material.BARRIER);
			ItemMeta bMeta = barrier.getItemMeta();
			bMeta.setDisplayName(MSG.color("&c&lNo Player Data"));
			bMeta.setLore(Arrays.asList(MSG.color("&7This player has never joined")));
			barrier.setItemMeta(bMeta);

			inv.setItem(inv.getSize() / 2, barrier);

			for (int i = 0; i < inv.getSize(); i++) {
				if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR)
					inv.setItem(i, glass);
			}
			return inv;
		}

		String lastIp = ScorchCore.getInstance().getDataManager().getScorchPlayer(uuid).getData("lastIp", String.class,
				"192.0.0.1");

		List<Punishment> history = ScorchCore.getInstance().getPunishModule().getPunishments(uuid);

		int friends = ScorchCore.getInstance().getModule("FriendModule", FriendModule.class).getFriends(uuid)
				.parallelStream().filter(f -> f.getStatus() == FriendStatus.FRIENDS).collect(Collectors.toList())
				.size();
		int directAlts = ScorchCore.getInstance().getModule("IPTrackerModule", IPTracker.class)
				.getAccountsWithIP(lastIp).size();
		int otherAlts = ScorchCore.getInstance().getModule("IPTrackerModule", IPTracker.class).linkedAccounts(uuid)
				.size() - 1;

		int gamesPlayed = 0;

		ReportModule rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);

		int reportsAgainst = rm.getReportsAgainst(uuid).size();
		int reportsSubmitted = rm.getReports(uuid).size();

		long playtime = ScorchCore.getInstance().getModule("PlaytimeModule", PlaytimeModule.class).getPlaytime(uuid);

		double seconds = playtime / 1000.0, minutes = seconds / 60.0;

		double punishmentScore = 0;

		for (Punishment p : history) {
			switch (p.getType()) {
				case BLACKLIST:
					punishmentScore += 100.0;
					break;
				case IP_BAN:
					punishmentScore += 90.0;
					break;
				case REPORT_BAN:
					punishmentScore += 40.0;
					break;
				case OTHER:
					punishmentScore += 20.0;
					break;
				case PERM_BAN:
					punishmentScore += 80.0;
					break;
				case PERM_MUTE:
					punishmentScore += 50.0;
					break;
				case TEMP_BAN:
					punishmentScore += Math.min((p.getDuration() / 1000.0 / 60.0 / 60.0) * 5.0, 100.0);
					break;
				case TEMP_MUTE:
					punishmentScore += Math.min((p.getDuration() / 1000.0 / 60.0) * 45.0, 100.0);
					break;
				case WARNING:
					punishmentScore += 10.0;
					break;
			}
		}

		double proV = Math.max(minutes + gamesPlayed * 5 + friends * 30, 1);
		double punishV = Math.max(punishmentScore + directAlts * 20 + otherAlts * 10, 1);
		double reportV = Math.max(reportsAgainst * 20 + reportsSubmitted * 10, 1);

		inv.setItem(0,
				new CItem(Material.CLOCK).name(MSG.color("&ePlaytime")).lore(MSG.color("&7" + minutes + "")).build());
		inv.setItem(1, new CItem(Material.GREEN_WOOL).name(MSG.color("&aGames Played"))
				.lore(MSG.color("&7" + gamesPlayed + " (*5)")).build());
		inv.setItem(2, new CItem(Material.PLAYER_HEAD).name(MSG.color("&2Friends"))
				.lore(MSG.color("&7" + friends + " (*30)")).build());
		inv.setItem(8,
				new CItem(Material.COAL_BLOCK).name(MSG.color("&eTotal")).lore(MSG.color("&7" + proV + "")).build());
		inv.setItem(9, new CItem(Material.DIAMOND_SWORD).name(MSG.color("&cPunishment Score"))
				.lore(MSG.color("&7" + punishmentScore + "")).build());
		inv.setItem(10, new CItem(Material.HOPPER).name(MSG.color("&dDirect Alts"))
				.lore(MSG.color("&7" + directAlts + " (*20)")).build());
		inv.setItem(11, new CItem(Material.BEDROCK).name(MSG.color("&5Indirect Alts"))
				.lore(MSG.color("&7" + otherAlts + " (*10)")).build());

		inv.setItem(17, new CItem(Material.IRON_BLOCK).name(MSG.color("&5Punishment Total"))
				.lore(MSG.color("&7" + punishV + "")).build());

		inv.setItem(18, new CItem(Material.BOOK).name(MSG.color("&3Reports Against"))
				.lore(MSG.color("&7" + reportsAgainst + " (*20)")).build());
		inv.setItem(19, new CItem(Material.WRITABLE_BOOK).name(MSG.color("&3Reports Submitted"))
				.lore(MSG.color("&7" + reportsSubmitted + " (*10)")).build());
		inv.setItem(26, new CItem(Material.REDSTONE_BLOCK).name(MSG.color("&6Report Total"))
				.lore(MSG.color("&7" + reportV + "")).build());
		inv.setItem(31, new CItem(Material.STICK).name(MSG.color("&e&lTotal"))
				.lore(MSG.color("&7" + proV / (punishV + reportV) + "")).build());

		for (int i = 0; i < inv.getSize(); i++) {
			if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR)
				inv.setItem(i, glass);
		}

		return inv;
	}


	public enum PublicTrust {
		ATROCIOUS("&4&lAtrocious", 0), TERRIBLE("&4&lTerrible", .1), VERY_LOW("&c&lVery Low", .2),
		MEDIOCRE("&c&lMediocre", .3), POOR("&6&lPoor", .4), OKAY("&6&lOkay", .5), AVERAGE("&6&lAverage", .6),
		DECENT("&e&lDecent", .7), GOOD("&e&lGood", .8), GREAT("&a&lGreat", .9), AMAZING("&a&lAmazing", .95);

		private double min;
		private String colored;

		PublicTrust(String colored, double min) {
			this.colored = colored;
			this.min = min;
		}

		public double getMin() {
			return min;
		}

		public String getColored() {
			return colored;
		}

		public static PublicTrust get(double d) {
			PublicTrust[] values = values();
			for (int i = values.length - 1; i >= 0; i--) {
				if (d >= values[i].getMin())
					return values[i];
			}
			return ATROCIOUS;
		}
	}

}
