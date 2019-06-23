package com.scorch.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.ConnectionManager;
import com.scorch.core.modules.data.DataManager;
import com.scorch.core.modules.punish.PunishModule;
import com.scorch.utils.Logger;

public class ScorchCore extends JavaPlugin {

	private static ScorchCore instance;

	private List<AbstractModule> modules;

	private File guiYml = new File(getDataFolder(), "guis.yml");

	private YamlConfiguration gui;

	private DataManager data;

	@Override
	public void onEnable() {
		instance = this;

		loadFiles();

		this.modules = new ArrayList<>();

		registerModule(new ConnectionManager("ConnectionManager"));
		registerModule(new PunishModule("PunishModule"));

		loadModules();
	}

	public DataManager getData() {
		return data;
	}

	@Override
	public void onDisable() {
		unloadModules();
	}

	public YamlConfiguration getGui() {
		return gui;
	}

	private void loadFiles() {
		gui = YamlConfiguration.loadConfiguration(guiYml);
	}

	private void loadModules() {
		getModules().forEach(AbstractModule::initialize);
	}

	private void unloadModules() {
		getModules().forEach(AbstractModule::disable);
	}

	public List<AbstractModule> getModules() {
		return modules;
	}

	public void registerModule(AbstractModule module) {
		if (!this.hasModule(module)) {
			this.modules.add(module);
		} else {
			Logger.warn("Module (" + module.getId() + ") already registered!");
		}
	}

	public boolean hasModule(AbstractModule module) {
		return modules.contains(module);
	}

	public static ScorchCore getInstance() {
		return instance;
	}
}
