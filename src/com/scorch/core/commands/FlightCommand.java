package com.scorch.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.scorch.core.utils.MSG;

public class FlightCommand implements CommandExecutor {

	public FlightCommand() {
		PluginCommand cmd = Bukkit.getPluginCommand("fly");
		cmd.setExecutor(this);
		cmd.setPermission("scorch.command.fly");
		cmd.setPermissionMessage("NOPE");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			MSG.tell(sender, "you must be a player");
			return true;
		}

		// very inefficient
		Player player = (Player) sender;
		if(args.length == 0)
		{
			if(!player.getAllowFlight())
			{
				player.setAllowFlight(true);
				player.sendMessage("flight on");
				return true;
			}else if(player.getAllowFlight())
			{
				player.setAllowFlight(false);
				player.sendMessage("flight off");
				return true;
			}
		}else if(args.length == 1 && player.hasPermission("scorch.command.fly.others"))
		{
			Player target = Bukkit.getPlayer(args[0]);
			
			if(!target.isFlying())
			{
				target.setAllowFlight(true);
				player.sendMessage("flight on for " + target.getName());
				// spacer
				target.sendMessage("flight turned on by " + player.getName());
				return true;
			}else if(target.isFlying())
			{
				target.setAllowFlight(false);
				player.sendMessage("flight off for " + target.getName());
				// spacer
				target.sendMessage("flight turned off by " + player.getName());
				return true;
			}
		}
		return false;
	}
}