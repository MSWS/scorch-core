package com.scorch.core.modules.staff;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.utils.MSG;

public class VanishModule extends AbstractModule {

	private List<Player> vanished;

	public VanishModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		vanished = new ArrayList<>();
	}

	@Override
	public void disable() {
		for (int i = 0; i < vanished.size(); i++)
			reveal(vanished.get(i));
		vanished.clear();
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
			if (!wouldSee(player, p))
				p.hidePlayer(ScorchCore.getInstance(), player);

		MSG.tell(player, " ");
		MSG.tell(player, "&4&l[&c&lVANISH&4&l] &9&lVANISH STATUS");
		MSG.tell(player, "&bYour vanish status has been &aenabled");
		MSG.tell(player, "&byou are now invisible to everyone except");
		MSG.tell(player, "&1your rank &band above.");
		MSG.tell(player, " ");

		vanished.add(player);
	}

	public void reveal(Player player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.showPlayer(ScorchCore.getInstance(), player);
		}

		MSG.tell(player, " ");
		MSG.tell(player, "&4&l[&c&lVANISH&4&l] &9&lVANISH STATUS");
		MSG.tell(player, "&bYour vanish status has been &cdisabled");
		MSG.tell(player, " ");

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
		vanished.remove(event.getPlayer());
	}
}
