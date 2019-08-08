package com.scorch.core.modules.scoreboard;

import org.bukkit.Bukkit;
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

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					setScoreboard(p);
				}
			}
		}.runTaskTimer(ScorchCore.getInstance(), 0, 1);
	}

	@Override
	public void disable() {

	}

	public void setScoreboard(Player player) {
		Scoreboard board = scoreboardManager.getNewScoreboard();
		Objective obj = board.registerNewObjective("ServerName", "dummy", MSG.color("&cScorch&6Gamez"));
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (int i = 1; i <= 15; i++) {
			String val = MSG.genUUID(8);
			Team team = board.registerNewTeam("team" + i);
			team.setPrefix(val);
			team.addEntry(""+i);
			obj.getScore(""+i).setScore(i);
		}
		player.setScoreboard(board);
	}
}