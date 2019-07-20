package com.scorch.core.modules.data;

import java.util.Map;
import java.util.UUID;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.exceptions.DataUpdateException;
import com.scorch.core.modules.permissions.PermissionPlayer;

public class ScorchPlayer {
	private UUID uuid;
	private PermissionPlayer pp;
	private Map<String, Object> data;

	public ScorchPlayer() {
	}

	public ScorchPlayer(UUID uuid, PermissionPlayer player, Map<String, Object> data) {
		this.uuid = uuid;
		this.pp = player;
		this.data = data;
	}

	public PermissionPlayer getPermissionPlayer() {
		return pp;
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

	public void saveData() {
		try {
			ScorchCore.getInstance().getDataManager().updateObject("players", this,
					new SQLSelector("uuid", uuid.toString()));
		} catch (DataUpdateException e) {
			e.printStackTrace();
		}
	}

}
