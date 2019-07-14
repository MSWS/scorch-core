package com.scorch.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.punish.Punishment;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.ConnectionManager;
import com.scorch.core.modules.data.DataManager;
import com.scorch.core.modules.punish.BanwaveModule;
import com.scorch.core.modules.punish.PunishModule;
import com.scorch.core.utils.Logger;

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

		// Data modules
		registerModule(new ConnectionManager("ConnectionManager"));
		registerModule(new DataManager("DataManager", (ConnectionManager)getModule("ConnectionManager")));
		this.data = (DataManager) getModule("DataManager");


		registerModule(new PunishModule("PunishModule"));
		registerModule(new BanwaveModule("BanwaveModule"));
		// DO NOT load ScoreboardModule

		loadModules();

		//Other initialisation after this

		try {
			getData().getAllObjects("test").forEach(obj -> {
				System.out.println("punished by: " + ((Punishment)obj).getStaffName());
			});
		} catch (DataObtainException e) {
			e.printStackTrace();
		}
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
		getConfig().options().copyDefaults(true);
		saveConfig();
		this.gui = YamlConfiguration.loadConfiguration(guiYml);
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

	public AbstractModule getModule (String id){
		for(AbstractModule module : getModules()){
			if(module.getId() == id){
				return module;
			}
		}
		return null;
	}

	public boolean hasModule(AbstractModule module) {
		return modules.contains(module);
	}

	public static ScorchCore getInstance() {
		return instance;
	}
}
