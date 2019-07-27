package com.scorch.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

/**
 * Improved Gamemmode command for utility usage
 * 
 * <b>Permissions</b><br>
 * scorch.command.gamemode.others - Access to change other player's
 * gamemodes<br>
 * scorch.command.gamemode.[GAMEMODE] - Access to specified gamemode
 * 
 * @author imodm
 *
 */
public class GamemodeCommand extends BukkitCommand {

	public GamemodeCommand(String name) {
		super(name);
		setPermission("scorch.command.gamemode");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("gm"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length < 1) {
			MSG.tell(sender, "/gamemode <Player> [Gamemode]");
			return true;
		}

		GameMode gm = null;

		Player target = null;

		if (args.length == 1) {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				MSG.tell(sender, "Specify player");
			}
			gm = getGamemode(args[0]);
		} else if (sender.hasPermission("scorch.command.gamemode.others")) {
			target = Bukkit.getPlayer(args[0]);
			gm = getGamemode(args[1]);
		} else {
			MSG.cTell(sender, "noperm");
			return true;
		}

		if (target == null) {
			MSG.tell(sender, "Unknown Player");
			return true;
		}

		if (gm == null) {
			MSG.tell(sender, "Unknown Gamemode");
			return true;
		}

		if (!sender.hasPermission("scorch.command.gamemode." + gm)) {
			MSG.cTell(sender, "noperm");
			return true;
		}

		target.setGameMode(gm);

		String msg = ScorchCore.getInstance().getMessage("gamemodeformat")
				.replace("%gamemode%", MSG.camelCase(gm.toString())).replace("%player%", target.getName())
				.replace("%s%", target.getName().toLowerCase().endsWith("s") ? "" : "s");
		MSG.tell(sender, msg);

		return true;
	}

	private GameMode getGamemode(String gm) {
		for (GameMode mode : GameMode.values()) {
			if (mode.toString().toLowerCase().startsWith(gm.toLowerCase()))
				return mode;
		}

		return null;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();

		for (GameMode mode : GameMode.values()) {
			if (sender.hasPermission("scorch.command.gamemode." + mode)
					&& mode.toString().toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
				result.add(mode.toString());
		}

		if (args.length == 1 && sender.hasPermission("scorch.command.gamemode.others")) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(p.getName());
			}
		}

		return result;
	}

}
