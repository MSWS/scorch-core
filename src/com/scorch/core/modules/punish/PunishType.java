package com.scorch.core.modules.punish;

import org.bukkit.Material;

import com.scorch.utils.Logger;
import com.scorch.utils.MSG;

public enum PunishType {
	IP_BAN("BEDROCK"), PERM_BAN("REDSTONE_BLOCK"), TEMP_BAN("DIAMOND_SWORD"), PERM_MUTE("WRITABLE_BOOK"),
	TEMP_MUTE("WRITABLE_BOOK"), WARNING("PAPER"), KICK("LEATHER_BOOTS"), OTHER("HOPPER");

	private Material mat;

	PunishType(String mat) {
		try {
			this.mat = Material.valueOf(mat);
		} catch (Exception e) {
			Logger.log("Unable to parse material " + mat);
		}
	}

	public Material getMaterial() {
		return mat;
	}
}
