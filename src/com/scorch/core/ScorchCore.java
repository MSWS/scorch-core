package com.scorch.core;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.ModulePriority;
import com.scorch.core.modules.chat.ChatModule;
import com.scorch.core.modules.chat.FilterModule;
import com.scorch.core.modules.data.CPlayer;
import com.scorch.core.modules.data.ConnectionManager;
import com.scorch.core.modules.data.DataManager;
import com.scorch.core.modules.messages.MessagesModule;
import com.scorch.core.modules.messages.OfflineMessagesModule;
import com.scorch.core.modules.permissions.PermissionModule;
import com.scorch.core.modules.punish.BanwaveModule;
import com.scorch.core.modules.punish.PunishModule;
import com.scorch.core.utils.Logger;

/**
 * The Core class of the plugin All initialisation is done here and it's used a
 * central piece of the plugin. You can get this instance by using
 * {@link ScorchCore#getInstance()} which you can use to access the modules
 *
 * @version 0.0.1
 */
public class ScorchCore extends JavaPlugin {

	private static ScorchCore instance;

	private Map<AbstractModule, ModulePriority> registeredModules;
	private Set<AbstractModule> modules;

	private DataManager dataManager;
	private MessagesModule messages;
	private PunishModule pMod;
	private FilterModule filter;

	private File guiYml = new File(getDataFolder(), "guis.yml");
	private YamlConfiguration gui;

	@Override
	public void onEnable() {
		instance = this;

		loadFiles();

		this.registeredModules = new HashMap<>();
		this.modules = new HashSet<>();

		// Data modules
		registerModule(new ConnectionManager("ConnectionManager"), ModulePriority.HIGHEST);
		dataManager = (DataManager) registerModule(
				new DataManager("DataManager", (ConnectionManager) getModule("ConnectionManager")),
				ModulePriority.HIGHEST);
		messages = (MessagesModule) registerModule(new MessagesModule("MessagesModule"), ModulePriority.HIGH);

		registerModule(new PermissionModule("PermissionModule"), ModulePriority.HIGH);
		pMod = (PunishModule) registerModule(new PunishModule("PunishModule"), ModulePriority.MEDIUM);
		registerModule(new BanwaveModule("BanwaveModule"), ModulePriority.MEDIUM);

		registerModule(new OfflineMessagesModule("OfflineMessagesModule"), ModulePriority.LOWEST);
		registerModule(new ChatModule("ChatModule"), ModulePriority.LOW);
		filter = (FilterModule) registerModule(new FilterModule("FilterModule"), ModulePriority.LOW);

		try {
			Arrays.stream(ModulePriority.values()).forEach(this::loadModules);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		// Other initialisation after this

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
			if (module.getId() == id) {
				return module;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link DataManager} object without having to use
	 * {@link ScorchCore#getModule(String)} and cast it This is purely to make it
	 * easier to write code using the dataManager manager
	 * 
	 * @return
	 */
	public DataManager getDataManager() {
		return dataManager;
	}

	/**
	 * @see getDataManager
	 * @return
	 */
	public MessagesModule getMessages() {
		return messages;
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
	
	public FilterModule getFilter() {
		return filter;
	}

	/**
	 * Returns a CPlayer from an OfflinePlayer
	 * 
	 * @param player
	 * @return
	 */
	public CPlayer getPlayer(OfflinePlayer player) {
		return dataManager.getPlayer(player);
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
}
