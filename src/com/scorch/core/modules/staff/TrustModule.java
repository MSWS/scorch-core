package com.scorch.core.modules.staff;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.players.FriendModule;
import com.scorch.core.modules.players.Friendship.FriendStatus;
import com.scorch.core.modules.players.IPTracker;
import com.scorch.core.modules.players.PlaytimeModule;
import com.scorch.core.modules.punish.Punishment;
import com.scorch.core.modules.report.ReportModule;

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
