package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.MSG;

public class SeenCommand extends BukkitCommand implements Listener {

	public SeenCommand(String name) {
		super(name);
		setPermission("scorch.command.seen");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));

		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length == 0) {
			MSG.tell(sender, "/seen [Player]");
			return true;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

		ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(target.getUniqueId());

		if (!sp.hasData("lastSeen")) {
			MSG.tell(sender, target.getName() + " has never joined the server before.");
			return true;
		}

		long last = System.currentTimeMillis() - sp.getData("lastSeen", Double.class).longValue();
		MSG.tell(sender, ScorchCore.getInstance().getMessage("seenformat").replace("%player%", target.getName())
				.replace("%time%", MSG.getTime(last)));
		return true;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		ScorchCore.getInstance().getDataManager().getScorchPlayer(event.getPlayer().getUniqueId()).setData("lastSeen",
				(Long) System.currentTimeMillis());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		ScorchCore.getInstance().getDataManager().getScorchPlayer(event.getPlayer().getUniqueId()).setData("lastSeen",
				(Long) System.currentTimeMillis());
	}

}
