package com.scorch.core.modules.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.scorch.core.utils.Logger;

public class CPlayer {
	private OfflinePlayer player;
	private UUID uuid;

	private HashMap<String, Object> tempData;

	private File saveFile, dataFile;
	private YamlConfiguration data;

	/**
	 * CPlayer is a custom player object that holds two different types of data
	 * temp: Temporary, do not use this for currencies, cooldowns, anything you want
	 * stored over reloads/restarts save: "Permanent", use this for currencies or
	 * anything you want saved note that these are completely separate, you cannot
	 * do {@link CPlayer#setSaveData(String, Object)} and expect to grab it with
	 * {@link CPlayer#getTempData(String)}
	 * 
	 * @param player OfflinePlayer to get data of, files are stored using UUID's
	 *               stripped of -'s
	 * @param plugin FreakyEnchants instance
	 */
	public CPlayer(OfflinePlayer player, JavaPlugin plugin) {
		this.player = player;
		this.uuid = player.getUniqueId();

		this.tempData = new HashMap<>();

		dataFile = new File(plugin.getDataFolder() + "/data");
		dataFile.mkdir();

		saveFile = new File(plugin.getDataFolder() + "/data/" + (uuid + "").replace("-", "") + ".yml");
		if (!saveFile.exists())
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		data = YamlConfiguration.loadConfiguration(saveFile);
	}

	public OfflinePlayer getPlayer() {
		return this.player;
	}

	public YamlConfiguration getDataFile() {
		return this.data;
	}

	public void setTempData(String id, Object obj) {
		tempData.put(id, obj);
	}

	@Deprecated
	public void setSaveData(String id, Object obj) {
		data.set(id, obj);
	}

	@Deprecated
	public void setSaveData(String id, Object obj, boolean save) {
		setSaveData(id, obj);
		if (save)
			saveData();
	}

	public void saveData() {
		try {
			data.save(saveFile);
		} catch (Exception e) {
			Logger.log("&cError saving data file");
			Logger.log("&a----------Start of Stack Trace----------");
			e.printStackTrace();
			Logger.log("&a----------End of Stack Trace----------");
		}
	}

	public void clearTempData() {
		tempData.clear();
	}

	public void clearSaveData() {
		saveFile.delete();
		saveFile.mkdir();
		data = YamlConfiguration.loadConfiguration(saveFile);
	}

	public Object getTempData(String id) {
		return tempData.get(id);
	}

	public String getTempString(String id) {
		return (String) getTempData(id);
	}

	public double getTempDouble(String id) {
		return hasTempData(id) ? (double) getTempData(id) : 0;
	}

	public int getTempInteger(String id) {
		return hasTempData(id) ? (int) getTempData(id) : 0;
	}

	public boolean hasTempData(String id) {
		return tempData.containsKey(id);
	}

	public Object getSaveData(String id) {
		return data.get(id);
	}

	public boolean hasSaveData(String id) {
		return data.contains(id);
	}

	public String getSaveString(String id) {
		return (String) getSaveData(id).toString();
	}

	@Deprecated
	/**
	 * Database use required
	 * 
	 * @param id
	 * @return
	 */
	public double getSaveDouble(String id) {
		return hasSaveData(id) ? (double) getSaveData(id) : 0;
	}

	public int getSaveInteger(String id) {
		return hasSaveData(id) ? (int) getSaveData(id) : 0;
	}

	public void removeTempData(String id) {
		tempData.remove(id);
	}

	public void removeSaveData(String id) {
		data.set(id, null);
	}

	public List<String> getTempEntries() {
		return new ArrayList<>(tempData.keySet());
	}

	public List<String> getSaveEntries() {
		return new ArrayList<>(data.getKeys(false));
	}
}
