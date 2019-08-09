package com.scorch.core.commands.staff;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;

public class FindCommand extends BukkitCommand {

	public FindCommand(String name) {
		super(name);
		setPermission("scorch.command.find");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("locate", "whereishe"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length == 0) {
			MSG.tell(sender, "/" + commandLabel + " [Player]");
			return true;
		}

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Forward"); // So BungeeCord knows to forward it
		out.writeUTF("ALL");
		out.writeUTF("Scorch"); // The channel name to check if this your data

		out.writeUTF("FIND " + args[0] + " " + sender.getName());

		Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

		player.sendPluginMessage(ScorchCore.getInstance(), "BungeeCord", out.toByteArray());
		return true;
	}

}
