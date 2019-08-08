package com.scorch.core.modules.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import com.scorch.core.ScorchCore;
import com.scorch.core.commands.FeedCommand;
import com.scorch.core.commands.FilterCommand;
import com.scorch.core.commands.FriendCommand;
import com.scorch.core.commands.HealCommand;
import com.scorch.core.commands.HelpCommand;
import com.scorch.core.commands.HistoryCommand;
import com.scorch.core.commands.LagCommand;
import com.scorch.core.commands.MessageCommand;
import com.scorch.core.commands.PingCommand;
import com.scorch.core.commands.PlaytimeCommand;
import com.scorch.core.commands.SeenCommand;
import com.scorch.core.commands.report.ConfirmReportCommand;
import com.scorch.core.commands.report.ReportCloseCommand;
import com.scorch.core.commands.report.ReportCommand;
import com.scorch.core.commands.report.ReportHandleCommand;
import com.scorch.core.commands.report.ReportInfoCommand;
import com.scorch.core.commands.staff.ACommand;
import com.scorch.core.commands.staff.BVersionCommand;
import com.scorch.core.commands.staff.BuildModeCommand;
import com.scorch.core.commands.staff.GamemodeCommand;
import com.scorch.core.commands.staff.MACommand;
import com.scorch.core.commands.staff.PunishCommand;
import com.scorch.core.commands.staff.RACommand;
import com.scorch.core.commands.staff.TFACommand;
import com.scorch.core.commands.staff.TPCommand;
import com.scorch.core.commands.staff.TestCommand;
import com.scorch.core.commands.staff.ToggleCommand;
import com.scorch.core.commands.staff.UnpunishCommand;
import com.scorch.core.commands.staff.VanishCommand;
import com.scorch.core.commands.staff.PermissionsCommand;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.utils.Logger;

/**
 * Module that manages all commands, use to enable/disable commands specifically
 * Still TODO
 * 
 * @author imodm
 *
 */
public class CommandModule extends AbstractModule {

	private Map<Command, Boolean> commands;

	private SimpleCommandMap map;

	public CommandModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		commands = new HashMap<>();

		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);
			map = (SimpleCommandMap) bukkitCommandMap.get(Bukkit.getServer());
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			Logger.error("Unable to get CommandMap");
			e.printStackTrace();
			return;
		}

		Logger.log("&9Enabling commands...");

		commands.put(new PermissionsCommand("permissions"), true);
		commands.put(new ACommand("a"), true);
		commands.put(new MACommand("ma"), true);
		commands.put(new RACommand("ra"), true);
		commands.put(new HistoryCommand("history"), true);
		commands.put(new PunishCommand("punish"), true);
		commands.put(new UnpunishCommand("unpunish"), true);
		commands.put(new TestCommand("test"), true);
		commands.put(new TPCommand("teleport"), true);
		commands.put(new VanishCommand("vanish"), true);
		commands.put(new HelpCommand("help"), true);
		commands.put(new FilterCommand("filter"), true);
		commands.put(new LagCommand("lag"), true);
		commands.put(new PingCommand("ping"), true);
		commands.put(new SeenCommand("seen"), true);
		commands.put(new PlaytimeCommand("playtime"), true);
		commands.put(new GamemodeCommand("gamemode"), true);
		commands.put(new FriendCommand("friend"), true);
		commands.put(new ToggleCommand("toggle"), true);
		commands.put(new BVersionCommand("buildversion"), true);
		commands.put(new BuildModeCommand("buildmode"), true);
		commands.put(new HealCommand("heal"), true);
		commands.put(new FeedCommand("feed"), true);
		commands.put(new ReportCommand("report"), true);
		commands.put(new ReportHandleCommand("reporthandle"), true);
		commands.put(new ReportCloseCommand("reportclose"), true);
		commands.put(new ReportInfoCommand("reportinfo"), true);
		commands.put(new ConfirmReportCommand("confirmreport"), true);
//		commands.put(new PermissionsCommand("permissions"), true);
		commands.put(new TFACommand("tfa"), true);
		commands.put(new MessageCommand("message"), true);
		commands.put(new PermissionsCommand("permissions"), true);

		enableCommands(commands.keySet().stream().collect(Collectors.toList()));
		Logger.log("&aSuccessfully enabled &e" + commands.size() + "&a command" + (commands.size() == 1 ? "" : "s"));
	}

	public void enableCommands(List<Command> commands) {
		commands.forEach(c -> enableCommand(c));
	}

	public void enableCommand(Command command) {
		map.register(ScorchCore.getInstance().getName(), command);
		commands.put(command, true);
	}

	public void disableCommands(List<Command> commands) {
		commands.forEach(cmd -> disableCommand(cmd));
	}

	public void disableCommand(Command cmd) {
		Iterator<Entry<String, Command>> it = getKnownCommands().entrySet().iterator();

		while (it.hasNext()) {
			Entry<String, Command> c = it.next();
			if (c.getValue().equals(cmd)) {
				it.remove();
			}
		}

		commands.put(cmd, false);
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Command> getKnownCommands() {
		try {
			final HashMap<String, Command> knownCommands = (HashMap<String, Command>) getPrivateField(map,
					"knownCommands");
			return knownCommands;
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Command getCommand(String command) {
		return commands.keySet().stream().filter(cmd -> cmd.getName().equals(command)).findFirst().orElse(null);
	}

	public boolean isEnabled(Command cmd) {
		return commands.getOrDefault(cmd, true);
	}

	public Map<Command, Boolean> getCommands() {
		return commands;
	}

	@Override
	public void disable() {
		disableCommands(new ArrayList<Command>(commands.keySet()));
	}

	/*
	 * Code from "zeeveener" at
	 * https://bukkit.org/threads/how-to-unregister-commands-from-your-plugin.
	 * 131808/ , edited by RandomHashTags
	 */
	private Object getPrivateField(Object object, String field)
			throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		Field objectField = field
				.equals("commandMap")
						? clazz.getDeclaredField(field)
						: field.equals("knownCommands")
								? Bukkit.getVersion().contains("1.13") ? clazz.getSuperclass().getDeclaredField(field)
										: clazz.getDeclaredField(field)
								: null;
		objectField.setAccessible(true);
		Object result = objectField.get(object);
		objectField.setAccessible(false);
		return result;
	}

}
