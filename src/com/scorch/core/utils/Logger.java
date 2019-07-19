package com.scorch.core.utils;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

public class Logger {

	private static void writeLog (String msg, String level) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', level + msg));
	}
	
	public static void log (String msg) {
		writeLog(msg, "[INFO] ");
	}

	public static void log (String msg, Object... args) { log(String.format(msg, args)); }

	public static void info (String msg) { log(msg); }

	public static void info (String msg, Object... args) { log(msg, args); }

	public static void warn (String msg) {
		writeLog(msg, "&e[WARNING] ");
	}

	public static void warn (String msg, Object... args) { warn(String.format(msg, args)); }

	public static void error (String msg) { writeLog(msg, "&c[ERROR] "); }

	public static void error (String msg, Object... args){ error(String.format(msg, args)); }
}
