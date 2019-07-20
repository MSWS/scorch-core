package com.scorch.core.modules.data;

import java.util.Map;
import java.util.UUID;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.exceptions.DataUpdateException;

public class ScorchPlayer {
	private UUID uuid;
	private Map<String, Object> data;

	public ScorchPlayer() {
	}

	public ScorchPlayer(UUID uuid, Map<String, Object> data) {
		this.uuid = uuid;
		this.data = data;
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

	public boolean hasData(String id) {
		return data.containsKey(id);
	}

	public Object getData(String id, Object defaultValue) {
		return data.containsKey(id) ? getData(id) : defaultValue;
	}

	public <T> T getData(String id, Class<T> cast, Object defaultValue) {
		return cast.cast(getData(id, defaultValue));
	}

	public void saveData() {
		try {
			ScorchCore.getInstance().getDataManager().updateObject("players", this,
					new SQLSelector("uuid", uuid.toString()));
		} catch (DataUpdateException e) {
			e.printStackTrace();
		}
	}

}
