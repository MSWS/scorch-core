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
	/**
	 * Bans the player's IP permanently
	 */
	IP_BAN("&d&lIP Ban", "BEDROCK", true),

	/*
	 * Permanently bans the player
	 */
	PERM_BAN("&4&lPerm Ban", "REDSTONE_BLOCK", true),

	/*
	 * Temporarily bans the player
	 */
	TEMP_BAN("&c&lTemp Ban", "DIAMOND_SWORD", true),

	/*
	 * Permanently mutes the player
	 */
	PERM_MUTE("&4&lPerm Mute", "BOOK", false),

	/*
	 * Temporarily mutes the player
	 */
	TEMP_MUTE("&c&lTemp Mute", "BOOK", false),

	/*
	 * Issues a warning to the player
	 */
	WARNING("&e&lWarning", "PAPER", false),

	/*
	 * Kicks the player
	 */
//	KICK("&b&lKick", "LEATHER_BOOTS", false),

	/*
	 * Blacklists the player Any and all accounts linked either via IP or accounts
	 * will be banned
	 */
	BLACKLIST("&4&lBlacklist", "BARRIER", true),

	/**
	 * 
	 */
	REPORT_BAN("&d&lReport Ban", "REDSTONE_LAMP", false),

	/*
	 * Miscellaneous
	 */
	OTHER("&3&lOther", "HOPPER", false);

	/*
	 * Material to represent the PunishType in an inventory
	 */
	private Material mat;

	/**
	 * Whether or not this will restrict a login once the player leaves/gets kicked
	 */
	private boolean restrictLogin;

	/**
	 * Gets a colored version to display in an inventory
	 */
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
