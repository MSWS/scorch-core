package com.scorch.core.modules.punish;

import org.bukkit.Material;

import com.scorch.core.utils.Logger;

/**
 * Defines a punishment type for a {@link Punishment}
 * 
 * @author imodm
 *
 */
public enum PunishType {
	IP_BAN("&d&lIP Ban", "BEDROCK", true), PERM_BAN("&4&lPerm Ban", "REDSTONE_BLOCK", true),
	TEMP_BAN("&c&lTemp Ban", "DIAMOND_SWORD", true), PERM_MUTE("&4&lPerm Mute", "BOOK", false),
	TEMP_MUTE("&c&lTemp Mute", "BOOK", false), WARNING("&e&lWarning", "PAPER", false),
	KICK("&b&lKick", "LEATHER_BOOTS", false), OTHER("&3&lOther", "HOPPER", false);

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
