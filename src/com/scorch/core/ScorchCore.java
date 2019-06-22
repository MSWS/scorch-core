package com.scorch.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.scorch.core.modules.AbstractModule;

public class ScorchCore extends JavaPlugin {
	
	private static ScorchCore instance;

	private List<AbstractModule> modules;

	@Override
	public void onEnable() {
		//Maybe do other stuff before loading all the modules
		instance = this;
		
		this.modules = new ArrayList<>();
		
		loadModules();
	}

	@Override
	public void onDisable() {
		unloadModules();
	}

	private void loadModules() {
		for(AbstractModule module : this.getModules()) {
			module.initialize();
		}
	}

	private void unloadModules() {
		for(AbstractModule module : this.getModules()) {
			module.disable();
		}
	}

	public List<AbstractModule> getModules() {
		return modules;
	}
	
	
	public void registerModule (AbstractModule module){
		if(!this.hasModule(module)) {
			this.modules.add(module);
		}
		else {
			//TODO: Replace this with logger class
			System.out.println("Module (" + module.getId() + ") already registered!");
		}
	}
	
	public boolean hasModule(AbstractModule module) {
		return modules.contains(module);
	}
	
	public static ScorchCore getInstance() {
		return instance;
	}
}
