package com.scortch.core;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.scortch.core.modules.Module;

public class ScortchCore extends JavaPlugin {
	private static ScortchCore plugin;

	private List<Module> modules;

	public static ScortchCore getPlugin() {
		return plugin;
	}

	@Override
	public void onEnable() {
		loadModules();
	}

	@Override
	public void onDisable() {
		unloadModules();
	}

	private void loadModules() {

	}

	private void unloadModules() {

	}

	public boolean hasModule(Module module) {
		return modules.contains(module);
	}
}
