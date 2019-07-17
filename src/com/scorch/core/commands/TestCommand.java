package com.scorch.core.commands;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.ConnectionManager;
import com.scorch.core.modules.messages.OfflineMessage;
import com.scorch.core.modules.messages.OfflineMessagesModule;
import com.scorch.core.utils.MSG;

public class TestCommand implements CommandExecutor, TabCompleter {

	public TestCommand() {
		PluginCommand cmd = Bukkit.getPluginCommand("test");
		cmd.setExecutor(this);
		cmd.setPermission("scorch.command.test");
		cmd.setPermissionMessage("NOPE");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			MSG.tell(sender, "/test [func] <args>");
			return true;
		}

		switch (args[0].toLowerCase()) {
		case "sql":
			if (args.length < 3) {
				MSG.tell(sender, "/test sql [Password] [SQL Statement]");
				return true;
			}

			final String pass = args[1];

			ConnectionManager mgr = ScorchCore.getInstance().getDataManager().getConnectionManager(pass);

			if (mgr == null) {
				MSG.tell(sender, "&cWrong password");
				return true;
			}

			StringBuilder builder = new StringBuilder();
			for (int i = 2; i < args.length; i++)
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
		case "message":
			MSG.tell(sender, ScorchCore.getInstance().getMessages().getMessage(args[1]));
			break;
		case "perm":
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
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> result = new ArrayList<String>();
		if (args.length <= 1) {
			for (String res : new String[] { "sql", "message", "perm", "offline" }) {
				if (res.toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(res);
			}
		}
		return result;
	}

}
