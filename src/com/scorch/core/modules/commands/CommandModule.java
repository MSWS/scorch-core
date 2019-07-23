package com.scorch.core.modules.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import com.scorch.core.ScorchCore;
import com.scorch.core.commands.ACommand;
import com.scorch.core.commands.FilterCommand;
import com.scorch.core.commands.FriendCommand;
import com.scorch.core.commands.GamemodeCommand;
import com.scorch.core.commands.HelpCommand;
import com.scorch.core.commands.HistoryCommand;
import com.scorch.core.commands.LagCommand;
import com.scorch.core.commands.MACommand;
import com.scorch.core.commands.PingCommand;
import com.scorch.core.commands.PlaytimeCommand;
import com.scorch.core.commands.PunishCommand;
import com.scorch.core.commands.RACommand;
import com.scorch.core.commands.SeenCommand;
import com.scorch.core.commands.TPCommand;
import com.scorch.core.commands.TestCommand;
import com.scorch.core.commands.UnpunishCommand;
import com.scorch.core.commands.VanishCommand;
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

		enableCommands(commands.keySet().stream().collect(Collectors.toList()));
	}

	public void enableCommands(List<Command> commands) {
		commands.forEach(c -> enableCommand(c));
	}

	public void enableCommand(Command command) {
		map.register(ScorchCore.getInstance().getName(), command);
	}

	public void disableCommands(List<Command> commands) {
		commands.forEach(cmd -> disableCommand(cmd));
	}

	public void disableCommand(Command cmd) {
		// TODO
	}

	public Command getCommand(String command) {
		return commands.keySet().stream().filter(cmd -> cmd.getName().equals(command)).findFirst().orElse(null);
	}

	@Override
	public void disable() {
		disableCommands(new ArrayList<Command>(commands.keySet()));
	}

}
