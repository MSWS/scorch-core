package com.scorch.core.modules.punish;

import org.bukkit.Material;

import com.scorch.core.utils.Logger;

public enum PunishType {
	IP_BAN("&d&lIP BAN", "BEDROCK", true), PERM_BAN("&4&lPERM BAN", "REDSTONE_BLOCK", true),
	TEMP_BAN("&c&lTEMP BAN", "DIAMOND_SWORD", true), PERM_MUTE("&4&lPERM MUTE", "BOOK", false),
	TEMP_MUTE("&c&lTEMP MUTE", "BOOK", false), WARNING("&e&lWARNING", "PAPER", false),
	KICK("&b&lKICK", "LEATHER_BOOTS", false), OTHER("&3&lOTHER", "HOPPER", false);

	private Material mat;
	private boolean restrictLogin;
	private String colored;

	PunishType(String colored, String mat, boolean restrictLogin) {
		this.colored = colored;
		this.restrictLogin = restrictLogin;
		try {
			this.mat = Material.valueOf(mat);
		} catch (Exception e) {
			Logger.log("Unable to parse material " + mat);
		}
	}

	public String getColored() {
		return colored;
	}

	public Material getMaterial() {
		return mat;
	}

	public boolean restrictsLogin() {
		return restrictLogin;
	}
}
