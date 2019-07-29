package com.scorch.core.commands;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.ConnectionManager;
import com.scorch.core.modules.messages.OfflineMessage;
import com.scorch.core.modules.messages.OfflineMessagesModule;
import com.scorch.core.modules.permissions.PermissionPlayer;
import com.scorch.core.modules.staff.TrustModule;
import com.scorch.core.modules.staff.TrustModule.PublicTrust;
import com.scorch.core.utils.MSG;

/**
 * Command used by developers and internally for <b>testing only</b>
 * 
 * <b>Permissions</b><br>
 * scorch.command.test - Access to command<br>
 * 
 * @author imodm
 *
 */
public class TestCommand extends BukkitCommand {

	public TestCommand(String name) {
		super(name);
		this.setPermission("scorch.command.test");
		this.setPermissionMessage("NOPE");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MSG.tell(sender, getPermissionMessage());
			return true;
		}

		if (args.length == 0) {
			MSG.tell(sender, "/test [func] <args>");
			return true;
		}

		Command cmd;
		switch (args[0].toLowerCase()) {
		case "reloadpunishments":
			ScorchCore.getInstance().getPunishModule().refreshPunishments();
			MSG.tell(sender, "Reloaded punishments");
			break;
		case "sql":
			if (!sender.hasPermission("core.sql"))
				return true;

			if (args.length < 2) {
				MSG.tell(sender, "/test sql [SQL Statement]");
				return true;
			}

			ConnectionManager mgr = (ConnectionManager) ScorchCore.getInstance().getModule("ConnectionManager");

			if (mgr == null) {
				MSG.tell(sender, "&cError getting connection manager!");
				return true;
			}

			StringBuilder builder = new StringBuilder();
			for (int i = 1; i < args.length; i++)
				builder.append(args[i] + " ");

			ResultSet set = mgr.executeQuery(builder.toString());
			if (set == null)
				break;
			try {
				ResultSetMetaData rsmd = set.getMetaData();
				List<String> columns = new ArrayList<>();
				List<String> values = new ArrayList<>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					columns.add(rsmd.getColumnLabel(i));
				}

				MSG.tell(sender, String.join(" | ", columns));
				int p = 0;
				while (set.next()) {
					values.add(p, "");
					for (int i = 1; i <= rsmd.getColumnCount(); i++) {
						values.set(p, values.get(p) + set.getObject(i) + " | ");
					}
					p++;
				}
				values.forEach(m -> MSG.tell(sender, m));

			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		case "reloadmessages":
			ScorchCore.getInstance().getMessages().reloadMessages();
			MSG.tell(sender, "Messages reloaded");
			break;
		case "message":
			MSG.tell(sender, ScorchCore.getInstance().getMessages().getMessage(args[1]));
			break;
		case "perm":
			PermissionPlayer pp = ScorchCore.getInstance().getPermissionModule().getPermissionPlayer((Player) sender);
			MSG.tell(sender, "Current Groups");
			for (String name : pp.getGroupNames()) {
				MSG.tell(sender, name);
			}
			MSG.tell(sender, "&7Have perm &e" + args[1] + "&7: " + MSG.TorF(sender.hasPermission(args[1])));
			break;
		case "offline":
			if (!(sender instanceof Player))
				return true;
			OfflineMessagesModule om = (OfflineMessagesModule) ScorchCore.getInstance()
					.getModule("OfflineMessagesModule");
			if (args.length < 2) {
				MSG.tell(sender, "/test offline [target] [message]");
				return true;
			}

			StringBuilder msg = new StringBuilder();
			for (int i = 2; i < args.length; i++) {
				msg.append(args[i] + " ");
			}

			OfflineMessage off = new OfflineMessage(sender.getName(), Bukkit.getOfflinePlayer(args[1]).getUniqueId(),
					msg.toString().trim());

			om.addMessage(off);
			break;
		case "enablecmd":
			if (args.length < 2) {
				MSG.tell(sender, "/test enablecmd [command]");
				return true;
			}

			cmd = ScorchCore.getInstance().getCommands().getCommand(args[1]);
			if (cmd == null) {
				MSG.tell(sender, "unknown command");
				return true;
			}
			ScorchCore.getInstance().getCommands().enableCommand(cmd);
			MSG.tell(sender, cmd.getName() + " enabled");
			break;
		case "disablecmd":
			if (args.length < 2) {
				MSG.tell(sender, "/test disablecmd [command]");
				return true;
			}

			cmd = ScorchCore.getInstance().getCommands().getCommand(args[1]);
			if (cmd == null) {
				MSG.tell(sender, "unknown command");
				return true;
			}
			ScorchCore.getInstance().getCommands().disableCommand(cmd);
			MSG.tell(sender, cmd.getName() + " disabled");
			break;
		case "trust":
			UUID target;
			if (args.length == 1) {
				if (sender instanceof Player) {
					target = ((Player) sender).getUniqueId();
				} else {
					MSG.tell(sender, "Specify Player.");
					return true;
				}
			} else {
				target = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
			}
			TrustModule tm = ScorchCore.getInstance().getModule("TrustModule", TrustModule.class);
			double trust = tm.getTrust(target);

			String name = Bukkit.getOfflinePlayer(target).getName();
			if (name == null)
				name = target.toString();

			MSG.tell(sender, name + "'" + (name.toLowerCase().endsWith("s") ? "" : "s") + ": "
					+ PublicTrust.get(trust).getColored() + " &7(&8" + trust + "&7)");
			break;
		default:
			MSG.tell(sender, "Unknown function");
			break;
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();
		if (args.length <= 1) {
			for (String res : new String[] { "sql", "message", "perm", "offline", "enablecmd", "disablecmd",
					"reloadmessages", "reloadpunishments", "trust" }) {
				if (res.toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(res);
			}
		}
		return result;
	}
}
