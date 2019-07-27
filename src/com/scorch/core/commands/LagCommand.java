package com.scorch.core.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.LagModule;
import com.scorch.core.utils.MSG;

/**
 * Get lag information
 * 
 * <b>Permissions</b><br>
 * scorch.command.lag - Access to command
 * 
 * @author imodm
 *
 */
public class LagCommand extends BukkitCommand {
	public LagCommand(String name) {
		super(name);
		setPermission("scorch.command.lag");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		Runtime rt = Runtime.getRuntime();

		Player player = null;
		int ping = 0;

		if (sender instanceof Player) {
			player = (Player) sender;
			try {
				Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
				ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException | NoSuchFieldException e) {
				e.printStackTrace();
			}
		}

		LagModule lag = ScorchCore.getInstance().getModule("LagModule", LagModule.class);

		List<String> tps = new ArrayList<>();
		for (int i : new int[] { 20, 20 * 60, 20 * 60 * 5, 20 * 60 * 60 }) {
			tps.add(MSG.getTPSColor((int) ScorchCore.getInstance().getTPS(i))
					+ MSG.parseDecimal(ScorchCore.getInstance().getTPS(i), 2));
		}

		MSG.tell(sender, " ");
		MSG.tell(sender, "&6&lServer Tick Information");
		MSG.tell(sender, "&8 * &7Uptime: &e" + MSG.getTime(lag.getUptime()));
		MSG.tell(sender, "&8 * &7TPS: " + String.join("&7, ", tps));
		if (sender instanceof Player)
			MSG.tell(sender, "&8 * &7Ping: &a" + MSG.getPingColor(ping) + ping);
		MSG.tell(sender,
				"&8 * &7Memory Usage: &a" + rt.freeMemory() / 1000000 + "&7/&c" + rt.maxMemory() / 1000000 + " &7(&8"
						+ ((MSG.parseDecimal((double) rt.freeMemory() / (double) rt.maxMemory() * 100, 2)) + "%&7)"));
		if (sender instanceof Player)
			MSG.tell(sender, "&8 * &7Loaded Chunks: &e" + player.getWorld().getLoadedChunks().length);
		if (sender instanceof Player)
			MSG.tell(sender,
					"&8 * &7Loaded Entities: &e"
							+ (player.getWorld().getEntities().size() - player.getWorld().getPlayers().size()) + "&d+"
							+ player.getWorld().getPlayers().size());
		MSG.tell(sender, " ");

		return true;
	}
}
