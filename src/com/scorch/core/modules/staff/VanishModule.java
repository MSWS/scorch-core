package com.scorch.core.modules.staff;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.MSG;

public class VanishModule extends AbstractModule implements Listener {

	private List<Player> vanished;

	public VanishModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		vanished = new ArrayList<>();

		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@Override
	public void disable() {
		for (int i = 0; i < vanished.size(); i++)
			reveal(vanished.get(i));
		vanished.clear();

		PlayerJoinEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
	}

	public boolean toggle(Player player) {
		if (isVanished(player))
			reveal(player);
		else
			vanish(player);
		return isVanished(player);
	}

	public void vanish(Player player) {
		for (Player p : Bukkit.getOnlinePlayers())
			if (!wouldSee(p, player))
				p.hidePlayer(ScorchCore.getInstance(), player);

		MSG.cTell(player, "vanishenablemessage");
		vanished.add(player);
	}

	public void reveal(Player player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.showPlayer(ScorchCore.getInstance(), player);
		}

		MSG.cTell(player, "vanishdisablemessage");

		vanished.remove(player);
	}

	public boolean isVanished(Player player) {
		return vanished.contains(player);
	}

	public boolean wouldSee(Player staff, Player target) {
		return getVanishLevel(staff) >= getVanishLevel(target);
	}

	public int getVanishLevel(Player player) {
		for (int i = 100; i > 0; i--) {
			if (player.hasPermission("scorch.vanish.level." + i))
				return i;
		}
		return -1;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(player.getUniqueId());

		if (sp.getData("vanished", Boolean.class, false)) {
			vanish(player);
		}

		for (Player staff : player.getWorld().getPlayers()) {
			if (!isVanished(staff))
				continue;
			if (wouldSee(player, staff))
				continue;

			player.hidePlayer(ScorchCore.getInstance(), staff);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(player.getUniqueId());
		sp.setData("vanished", isVanished(player));

		vanished.remove(player);
	}
}
