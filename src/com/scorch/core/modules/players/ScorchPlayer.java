package com.scorch.core.modules.players;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.modules.data.exceptions.DataUpdateException;

public class ScorchPlayer {
	private UUID uuid;
	private String username;
	private Map<String, Object> data;
	@DataIgnore
	private Map<String, Object> tempData;

	public ScorchPlayer() {
		this.tempData = new HashMap<String, Object>();
	}

	public ScorchPlayer(UUID uuid, String username, Map<String, Object> data) {
		this.uuid = uuid;
		this.username = username;

		this.data = data;
		this.tempData = new HashMap<String, Object>();
	}

	public Map<String, Object> getData() {
		return data;
	}

	public Object getData(String id) {
		return data.get(id);
	}

	public <T> T getData(String id, Class<T> cast) {
		return cast.cast(getData(id));
	}

	public void setData(String id, Object obj) {
		data.put(id, obj);
	}

	public boolean removeData(String id) {
		return data.remove(id) != null;
	}

	public boolean hasData(String id) {
		return data.containsKey(id);
	}

	public Object getData(String id, Object defaultValue) {
		return data.containsKey(id) ? getData(id) : defaultValue;
	}

	public <T> T getData(String id, Class<T> cast, Object defaultValue) {
		return cast.cast(getData(id, defaultValue));
	}

	/**
	 * Updates the database with the current {@link ScorchPlayer} info
	 */
	public void saveData() {
		try {
			ScorchCore.getInstance().getDataManager().updateObject("players", this,
					new SQLSelector("uuid", uuid.toString()));
		} catch (DataUpdateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the username
	 */
	public String getName() {
		return username;
	}

	public void setName(String name) {
		this.username = name;
	}

	public void setTempData(String key, Object value) {
		tempData.put(key, value);
	}

	public Object getTempData(String key) {
		return getTempData(key, Object.class);
	}

	public <T> T getTempData(String key, Class<T> cast) {
		return getTempData(key, cast, null);
	}

	public <T> T getTempData(String key, Class<T> cast, Object def) {
		return cast.cast(tempData.getOrDefault(key, def));
	}

	public boolean hasTempData(String key) {
		return tempData.containsKey(key);
	}

	public Map<String, Object> getTempData() {
		return tempData;
	}

	public boolean removeTempData(String key) {
		return tempData.remove(key) != null;
	}
}
