package com.scorch.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.ModulePriority;
import com.scorch.core.modules.chat.ChatModule;
import com.scorch.core.modules.chat.FilterModule;
import com.scorch.core.modules.combat.CombatModule;
import com.scorch.core.modules.commands.CommandModule;
import com.scorch.core.modules.communication.CommunicationModule;
import com.scorch.core.modules.data.ConnectionManager;
import com.scorch.core.modules.data.DataManager;
import com.scorch.core.modules.data.LagModule;
import com.scorch.core.modules.economy.EconomyModule;
import com.scorch.core.modules.messages.MessagesModule;
import com.scorch.core.modules.messages.OfflineMessagesModule;
import com.scorch.core.modules.permissions.PermissionModule;
import com.scorch.core.modules.permissions.PermissionPlayer;
import com.scorch.core.modules.players.FriendModule;
import com.scorch.core.modules.players.IPTracker;
import com.scorch.core.modules.players.PlaytimeModule;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.punish.BanwaveModule;
import com.scorch.core.modules.punish.PunishModule;
import com.scorch.core.modules.punish.tests.PunishEventTest;
import com.scorch.core.modules.report.ReportModule;
import com.scorch.core.modules.scoreboard.ScoreboardModule;
import com.scorch.core.modules.staff.AuthenticationModule;
import com.scorch.core.modules.staff.BuildModeModule;
import com.scorch.core.modules.staff.PlayerCombatModule;
import com.scorch.core.modules.staff.TeleportModule;
import com.scorch.core.modules.staff.TrustModule;
import com.scorch.core.modules.staff.VanishModule;
import com.scorch.core.modules.staff.WorldProtectionModule;
import com.scorch.core.pastebin.Paste;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.MSG;

/**
 * The Core class of the plugin All initialisation is done here and it's used a
 * central piece of the plugin. You can get this instance by using
 * {@link ScorchCore#getInstance()} which you can use to access the modules
 *
 * @version 0.0.1
 */
public class ScorchCore extends JavaPlugin implements PluginMessageListener {

	private static ScorchCore instance;

	private Map<AbstractModule, ModulePriority> registeredModules;
	private Set<AbstractModule> modules;

	private DataManager dataManager;
	private PermissionModule permissionModule;
	private MessagesModule messages;
	private PunishModule pMod;
	private CommandModule commands;
	private CommunicationModule communicationModule;

	private EconomyModule economy;

	private File guiYml = new File(getDataFolder(), "guis.yml");
	private YamlConfiguration gui;

	private String serverName;

	@Override
	public void onEnable() {
		instance = this;

		loadFiles();

		Paste.setDeveloperKey(getConfig().getString("PastebinKey"));

		this.registeredModules = new HashMap<>();
		this.modules = new HashSet<>();

		// Data modules
		this.communicationModule = (CommunicationModule) registerModule(new CommunicationModule("CommunicationModule"),
				ModulePriority.HIGHEST);
		registerModule(new ConnectionManager("ConnectionManager"), ModulePriority.HIGHEST);
		this.dataManager = (DataManager) registerModule(
				new DataManager("DataManager", (ConnectionManager) getModule("ConnectionManager")),
				ModulePriority.HIGHEST);

		this.messages = (MessagesModule) registerModule(new MessagesModule("MessagesModule"), ModulePriority.HIGH);
		this.permissionModule = (PermissionModule) registerModule(new PermissionModule("PermissionModule"),
				ModulePriority.HIGH);
		registerModule(new BuildModeModule("BuildModeModule"), ModulePriority.HIGH);
		registerModule(new WorldProtectionModule("WorldProtectionModule"), ModulePriority.HIGH);
		registerModule(new PlayerCombatModule("PlayerCombatModule"), ModulePriority.HIGH);
		registerModule(new ScoreboardModule("ScoreboardModule"), ModulePriority.HIGH);

		registerModule(new IPTracker("IPTrackerModule"), ModulePriority.MEDIUM);
		registerModule(new BanwaveModule("BanwaveModule"), ModulePriority.MEDIUM);
		registerModule(new OfflineMessagesModule("OfflineMessagesModule"), ModulePriority.MEDIUM);
		registerModule(new ReportModule("ReportModule"), ModulePriority.MEDIUM);
		this.pMod = (PunishModule) registerModule(new PunishModule("PunishModule"), ModulePriority.MEDIUM);
		this.commands = (CommandModule) registerModule(new CommandModule("CommandModule"), ModulePriority.MEDIUM);
		this.economy = (EconomyModule) registerModule(new EconomyModule("EconomyModule"), ModulePriority.MEDIUM);

		registerModule(new ChatModule("ChatModule"), ModulePriority.LOW);
		registerModule(new TeleportModule("TeleportModule"), ModulePriority.LOW);
		registerModule(new CombatModule("CombatModule"), ModulePriority.LOW);
		registerModule(new VanishModule("VanishModule"), ModulePriority.LOW);
		registerModule(new FilterModule("FilterModule"), ModulePriority.LOW);
		registerModule(new FriendModule("FriendModule"), ModulePriority.LOW);
		registerModule(new AuthenticationModule("AuthenticationModule"), ModulePriority.LOW);

		registerModule(new LagModule("LagModule"), ModulePriority.LOWEST);
		registerModule(new PlaytimeModule("PlaytimeModule"), ModulePriority.LOWEST);
		registerModule(new TrustModule("TrustModule"), ModulePriority.LOWEST);

		try {
			Arrays.stream(ModulePriority.values()).forEach(this::loadModules);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (serverName != null)
					cancel();
				Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
				if (p == null)
					return;

				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("GetServer");

				p.sendPluginMessage(ScorchCore.getInstance(), "BungeeCord", out.toByteArray());
			}
		}.runTaskTimer(ScorchCore.getInstance(), 0, 20 * 10);

		new BukkitRunnable() {
			@Override
			public void run() {
				new PunishEventTest();
			}
		}.runTaskLater(this, 100); // Run 5 seconds after boot to ensure visibility in console

	}

	@Override
	public void onDisable() {
		unloadModules();
	}

	/**
	 * Loads the required files from the disk, this also sets up the config file.
	 */
	private void loadFiles() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		saveResource("guis.yml", false);

		this.guiYml = new File(getDataFolder(), "guis.yml");
		this.gui = YamlConfiguration.loadConfiguration(guiYml);
	}

	/**
	 * Calls {@link AbstractModule#initialize()} on all the registered modules with
	 * the supplied {@link ModulePriority}
	 * 
	 * @param priority the module priority to initialise
	 *
	 * @see ModulePriority
	 */
	private void loadModules(ModulePriority priority) {
		getRegisteredModules().forEach((module, modulePriority) -> {
			if (modulePriority == priority) {
				module.initialize();
			}
		});
	}

	/**
	 * Calls {@link AbstractModule#disable()} on all the registered modules.
	 */
	private void unloadModules() {
		getModules().forEach(AbstractModule::disable);
	}

	public void disableModule(AbstractModule module) {
		modules.remove(module);
		module.disable();
	}

	/**
	 * Get the registered modules.
	 * 
	 * @return the registered modules
	 */
	public Set<AbstractModule> getModules() {
		return modules;
	}

	/**
	 * Gets the list of registered modules that need to be initialised. They will be
	 * registered based on their {@link ModulePriority}
	 * 
	 * @return the list of registered modules
	 *
	 * @see ModulePriority
	 */
	private Map<AbstractModule, ModulePriority> getRegisteredModules() {
		return registeredModules;
	}

	/**
	 * Registers a module to be initialised. Modules will be initialised using
	 * {@link ModulePriority}.
	 * 
	 * @param module   the module to register
	 * @param priority the module's priority
	 *
	 * @see ModulePriority
	 */
	public AbstractModule registerModule(AbstractModule module, ModulePriority priority) {
		if (this.modules.add(module)) {
			this.getRegisteredModules().put(module, priority);
		} else {
			Logger.warn("Module (" + module.getId() + ") is already registered!");
		}
		return modules.stream().filter(m -> m == module).findFirst().orElse(null);
	}

	/**
	 * Gets the module from the registered modules. Returns null if the module
	 * doesn't exist, so make sure the id is correct and the module exists
	 * 
	 * @param id the module id to use
	 * @return the module
	 */
	public AbstractModule getModule(String id) {
		for (AbstractModule module : getModules()) {
			if (module.getId().equals(id)) {
				return module;
			}
		}

		Logger.warn("Unknown Module: %s", id);
		return null;
	}

	public <T extends AbstractModule> T getModule(String id, Class<T> cast) {
		return cast.cast(getModule(id));
	}

	/**
	 * Returns the {@link DataManager} object without having to use
	 * {@link ScorchCore#getModule(String)} and casting it This is purely to make it
	 * easier to write code using the {@link DataManager}
	 * 
	 * @see DataManager
	 * @return the datamanager
	 */
	public DataManager getDataManager() {
		return dataManager;
	}

	/**
	 * Returns the {@link EconomyModule} object without having to use
	 * {@link ScorchCore#getModule(String)} and casting it. This is purely to make
	 * it easier to write code using the {@link EconomyModule}
	 *
	 * @see EconomyModule
	 * @return the economy module
	 */
	public EconomyModule getEconomy() {
		return economy;
	}

	/**
	 * Returns the {@link CommunicationModule} object without having to use
	 * {@link ScorchCore#getModule(String)} and casting it, This is purely to make
	 * it easier to write code using the {@link CommunicationModule}
	 *
	 * @see CommunicationModule
	 * @return the communication module
	 */
	public CommunicationModule getCommunicationModule() {
		return communicationModule;
	}

	public MessagesModule getMessages() {
		return messages;
	}

	public String getMessage(String id) {
		return messages.getMessage(id).getMessage();
	}

	/**
	 * Returns the {@link PermissionModule} object without having to use
	 * {@link ScorchCore#getModule(String)} and cast it. This is purely to make it
	 * easier to write code using the {@link PermissionModule}
	 * 
	 * @see PermissionModule
	 *
	 * @return the permission module
	 */
	public PermissionModule getPermissionModule() {
		return permissionModule;
	}

	/**
	 * Gets the GUI yml configuration
	 * 
	 * @return the GUI yml configuration
	 */
	public YamlConfiguration getGui() {
		return gui;
	}

	public PunishModule getPunishModule() {
		return pMod;
	}

	public CommandModule getCommands() {
		return commands;
	}

	/**
	 * Gets the prefix of the player from their primary group
	 * 
	 * @param player
	 * @return null if not assigned to any group
	 */
	public String getPrefix(UUID player) {
		PermissionPlayer pp = permissionModule.getPermissionPlayer(player);
		if (pp == null) {
			Logger.warn("perm player null for %s", player);
			return "";
		}
		return permissionModule.getPermissionPlayer(player).getPrimaryGroup() == null ? ""
				: permissionModule.getPermissionPlayer(player).getPrimaryGroup().getPrefix();
	}

	/**
	 * {@link ScorchCore#getPrefix(UUID)}
	 * 
	 * @param player
	 * @return
	 */
	public String getPrefix(OfflinePlayer player) {
		return getPrefix(player.getUniqueId());
	}

	public ScorchPlayer getPlayer(UUID uuid) {
		return dataManager.getScorchPlayer(uuid);
	}

	/**
	 * Returns true if the {@link AbstractModule} exists.
	 * 
	 * @param module the module to check
	 * @return whether the module exists
	 */
	public boolean hasModule(AbstractModule module) {
		return modules.contains(module);
	}

	/**
	 * Gets the instance of {@link JavaPlugin} used by the core plugin, this can be
	 * used from other plugins building on this plugin as well to get access to
	 * useful modules such as the {@link DataManager} and {@link PermissionModule}.
	 * 
	 * @return the plugin instance
	 */
	public static ScorchCore getInstance() {
		return instance;
	}

	public double getTPS(int ticks) {
		return ((LagModule) getModule("LagModule")).getTPS(ticks);
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord"))
			return;
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		Logger.log("Received message: " + subchannel);

		switch (subchannel) {
		case "Scorch":
			String method = in.readUTF();
			Logger.log("method: " + method);
			switch (method.split(" ")[0]) {
			case "MESSAGE":
				Player target = Bukkit.getPlayer(method.split(" ")[1]);
				if (target == null)
					return;
				String msg = "";
				for (int i = 2; i < method.split(" ").length; i++) {
					msg += method.split(" ")[i] + " ";
				}

				msg = msg.trim();

				MSG.tell(target, msg);
				break;
			case "FIND":
				Player found = Bukkit.getPlayer(method.split(" ")[1]);
				if (found == null)
					return;

				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Forward");
				out.writeUTF("ALL");
				out.writeUTF("Scorch");
				out.writeUTF("MESSAGE " + method.split(" ")[2] + " Found on server " + serverName + "!");

				found.sendPluginMessage(this, "BungeeCord", out.toByteArray());
				break;
			default:
				Logger.log("Unknown method: " + method);
//				String subChannel = in.readUTF();
				short len = in.readShort();
				byte[] msgbytes = new byte[len];
				in.readFully(msgbytes);

				DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
				try {
					String somedata = msgin.readUTF();
					short somenumber = msgin.readShort();
					Logger.log("data: " + somedata + " num: " + somenumber);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Read the data in the same way you wrote it
				break;
			}
			break;
		case "GetServer":
			this.serverName = in.readUTF();
			Logger.log("Received server name: " + serverName);
			break;
		default:
			Logger.log("Unknown channel: " + subchannel);
			break;
		}
	}
}
