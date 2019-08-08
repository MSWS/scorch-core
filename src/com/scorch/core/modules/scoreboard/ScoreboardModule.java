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
import com.scorch.core.utils.MSG;

/**
 * This is the main scoreboard utility that handles scoreboards across the
 * servers
 * 
 * @author Gijs "kitsune" de Jong
 *
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
				for (Player p : Bukkit.getOnlinePlayers()) {
					for (int i = 1; i <= 15; i++) {
						setLine(p, i, "test");
					}
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

		String pre = value.substring(0, Math.min(64, value.length())), suff = value
				.substring(Math.min(value.length(), 64), Math.max(value.length(), Math.min(value.length(), 64)));

		ChatColor[] vals = ChatColor.values();

		Team team = board.getTeam(vals[line] + "" + ChatColor.RESET);
		if (team == null)
			team = board.registerNewTeam(vals[line] + "" + ChatColor.RESET);

		team.setPrefix(pre);
		team.setSuffix(suff);
		team.addEntry(vals[line] + "" + ChatColor.RESET);
		obj.getScore(vals[line] + "" + ChatColor.RESET).setScore(line);
	}
}