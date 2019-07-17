package com.scorch.core.modules.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;

public class CPlayer {
	private OfflinePlayer player;

	private Map<String, Object> tempData;

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
	 */
	public CPlayer(OfflinePlayer player) {
		this.player = player;

		this.tempData = new HashMap<>();
	}

	public OfflinePlayer getPlayer() {
		return this.player;
	}

	public void setTempData(String id, Object obj) {
		tempData.put(id, obj);
	}

	public void clearTempData() {
		tempData.clear();
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

	public void removeTempData(String id) {
		tempData.remove(id);
	}

	public List<String> getTempEntries() {
		return new ArrayList<>(tempData.keySet());
	}

	public Map<String, Object> getTempMap() {
		return tempData;
	}

	public <T> T getTempData(String id, Class<T> cast) {
		return cast.cast(getTempData(id));
	}
}
