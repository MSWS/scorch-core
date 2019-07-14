package com.scorch.core.modules.punish;

import org.bukkit.Material;

import com.scorch.core.utils.Logger;

public enum PunishType {
	IP_BAN("BEDROCK", true), PERM_BAN("REDSTONE_BLOCK", true), TEMP_BAN("DIAMOND_SWORD", true),
	PERM_MUTE("WRITABLE_BOOK", false), TEMP_MUTE("WRITABLE_BOOK", false), WARNING("PAPER", false),
	KICK("LEATHER_BOOTS", false), OTHER("HOPPER", false);

	private Material mat;
	private boolean restrictLogin;

	PunishType(String mat, boolean restrictLogin) {
		this.restrictLogin = restrictLogin;
		try {
			this.mat = Material.valueOf(mat);
		} catch (Exception e) {
			Logger.log("Unable to parse material " + mat);
		}
	}

	public Material getMaterial() {
		return mat;
	}

	public boolean restrictsLogin() {
		return restrictLogin;
	}
}
