package com.scorch.core.modules.scoreboard;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.staff.TrustModule;
import com.scorch.core.utils.MSG;

/**
 * This is the main scoreboard utility that handles scoreboards across the
 * servers
 */
public class ScoreboardModule extends AbstractModule {

	private ScoreboardManager scoreboardManager;

	public ScoreboardModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		this.scoreboardManager = Bukkit.getScoreboardManager();

		for (Player p : Bukkit.getOnlinePlayers())
			p.setScoreboard(scoreboardManager.getNewScoreboard());

		new BukkitRunnable() {
			@Override
			public void run() {
				int online = ScorchCore.getInstance().getCommunicationModule().getNetworkOnlinePlayers().size();
				for (Player p : Bukkit.getOnlinePlayers()) {
					setLine(p, 15, "&aWelcome to &cScorch&6Gamez&e!");
					setLine(p, 14, " ");
					setLine(p, 13, " ");
					setLine(p, 12, " ");
					setLine(p, 11, "&eCurrently online: &e" + online);
					setLine(p, 10, " ");
					setLine(p, 9, " ");
					setLine(p, 8, "&cScorch&6Bux&e: " + ScorchCore.getInstance().getEconomy().getFunds(p));
					setLine(p, 7, " ");
					setLine(p, 6, " ");
					setLine(p, 5, " ");
					setLine(p, 4, " ");
					setLine(p, 3, "");
					setLine(p, 2, ScorchCore.getInstance().getModule("TrustModule", TrustModule.class)
							.getTrust(p.getUniqueId()) + "");
					setLine(p, 1, "&escorchgamez.net");

				}
			}
		}.runTaskTimer(ScorchCore.getInstance(), 0, 1);
	}

	@Override
	public void disable() {

	}

	public void setLine(Player player, int line, String value) {
		Scoreboard board = player.getScoreboard();
		Objective obj;
		if (board == null || board.getObjective("scorchboard") == null) {
			board = scoreboardManager.getNewScoreboard();
			player.setScoreboard(board);

			obj = board.registerNewObjective("scorchboard", "dummy", MSG.color("&cScorch&6Gamez"));
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		obj = board.getObjective("scorchboard");

		Validate.isTrue(value.length() <= 124, "Value cannot exceed length of 124", value);

		String prefix = ChatColor.translateAlternateColorCodes('&', value.substring(0, Math.min(62, value.length())));
		String suffix = ChatColor.translateAlternateColorCodes('&',
				value.substring(Math.min(value.length(), 62), Math.max(value.length(), Math.min(value.length(), 62))));

		ChatColor[] vals = ChatColor.values();

		Team team = board.getTeam(vals[line] + "" + ChatColor.RESET);
		if (team == null)
			team = board.registerNewTeam(vals[line] + "" + ChatColor.RESET);

		team.setPrefix(prefix);
		team.setSuffix(suffix);
		team.addEntry(vals[line] + "" + ChatColor.RESET);
		obj.getScore(vals[line] + "" + ChatColor.RESET).setScore(line);
	}
}