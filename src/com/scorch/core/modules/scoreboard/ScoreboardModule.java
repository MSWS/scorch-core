package com.scorch.core.modules.scoreboard;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

/**
 * This is the main scoreboard utility that handles scoreboards across the servers
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
				for(Player p : Bukkit.getOnlinePlayers()){
					setScoreboard(p);
				}
			}
		}.runTaskTimer(ScorchCore.getInstance(), 0 ,5);
	}

	@Override
	public void disable() {

	}

	public void setScoreboard (Player player){
		Scoreboard board = scoreboardManager.getNewScoreboard();
		Objective obj = board.registerNewObjective("ServerName", "dummy");
		obj.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cScorch&6Gamez"));
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

		for(int i = 0; i < 15; i++){
			String val = StringUtils.getUniqueString(10);
			Logger.info("val: %s", val);
			Team team = board.registerNewTeam("team" + i);
			team.setPrefix(val);
			team.addEntry("team" + i);
			obj.getScore("team" + i).setScore(i);
		}
		player.setScoreboard(board);
	}
}