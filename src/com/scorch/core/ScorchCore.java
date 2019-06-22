package com.scorch.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.ConnectionManager;
import com.scorch.utils.Logger;

public class ScorchCore extends JavaPlugin {

	private static ScorchCore instance;

	private List<AbstractModule> modules;
	
	@Override
	public void onEnable() {
		instance = this;

		this.modules = new ArrayList<>();

		this.registerModule(new ConnectionManager("DataManager"));

		loadModules();
	}

	@Override
	public void onDisable() {
		unloadModules();
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
