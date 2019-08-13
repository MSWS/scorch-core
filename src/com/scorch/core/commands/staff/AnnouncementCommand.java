package com.scorch.core.commands.staff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.scorch.core.ScorchCore;
import com.scorch.core.events.announcements.AnnouncementSendEvent;
import com.scorch.core.modules.communication.exceptions.WebSocketException;
import com.scorch.core.utils.MSG;

public class AnnouncementCommand extends Command {

	public AnnouncementCommand(String name) {
		super(name);
		setPermission("scorch.command.announce");
		setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
		setAliases(Arrays.asList("broadcast"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length < 2) {
			MSG.tell(sender, "/" + commandLabel + " [server/here/all] <perm:permission> [Message]");
			return true;
		}

		String server = args[0], msg = "", perm = null;
		if (!sender.hasPermission("scorch.command.announce." + server)) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		for (int i = 1; i < args.length; i++) {
			if (args[i].toLowerCase().startsWith("perm:")) {
				perm = args[i].substring("perm:".length());
				continue;
			}
			msg += args[i] + " ";
		}
		msg = msg.trim();

		if (msg.isEmpty()) {
			MSG.tell(sender, "/" + commandLabel + " [server/here/all] <perm:permission> [Message]");
			return true;
		}

		AnnouncementSendEvent ase = new AnnouncementSendEvent(server, perm, msg);

		if (server.equalsIgnoreCase(ScorchCore.getInstance().getServerName()) || server.equalsIgnoreCase("here")) {
			if (server.equalsIgnoreCase("here"))
				ase.setServer(ScorchCore.getInstance().getServerName());

			Bukkit.getPluginManager().callEvent(ase);
			return true;
		}

		if (server.equalsIgnoreCase("all"))
			Bukkit.getPluginManager().callEvent(ase);

		try {
			ScorchCore.getInstance().getCommunicationModule().dispatchEvent(ase);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();
		if (args.length > 1)
			return result;
		for (String res : new String[] { "here", "all" }) {
			if (res.toLowerCase().startsWith(args[0].toLowerCase()))
				result.add(res);
		}
		for (String res : ScorchCore.getInstance().getCommunicationModule().getServers()) {
			if (res.toLowerCase().startsWith(args[0].toLowerCase()))
				result.add(res);
		}
		if (ScorchCore.getInstance().getServerName().toLowerCase().startsWith(args[0]))
			result.add(ScorchCore.getInstance().getServerName());

		return result;
	}

}
