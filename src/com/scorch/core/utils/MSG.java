package com.scorch.core.utils;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MSG {
	public static JavaPlugin plugin;

	/**
	 * Returns the string with &'s being �
	 * 
	 * @param msg the message to replace
	 * @return returns colored msg
	 */
	public static String color(String msg) {
		if (msg == null || msg.isEmpty())
			return null;
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	/**
	 * Returns string with camel case, and with _'s replaced with spaces
	 * 
	 * @param string hello_how is everyone
	 * @return Hello How Is Everyone
	 */
	public static String camelCase(String string) {
		String prevChar = " ";
		String res = "";
		for (int i = 0; i < string.length(); i++) {
			if (i > 0)
				prevChar = string.charAt(i - 1) + "";
			if (prevChar.matches("[a-zA-Z]")) {
				res = res + ((string.charAt(i) + "").toLowerCase());
			} else {
				res = res + ((string.charAt(i) + "").toUpperCase());
			}
		}
		return res.replace("_", " ");
	}

	/**
	 * Sends an object to the sender (supports Collections)
	 * 
	 * @param sender
	 * @param msg
	 */
	public static void tell(CommandSender sender, Object msg) {
		if (msg == null)
			return;
		if (msg instanceof Collection<?>) {
			((Collection<?>) msg).forEach((obj) -> tell(sender, obj));
		} else if (msg instanceof Object[]) {
			for (Object obj : (Object[]) msg)
				tell(sender, obj);
		} else {
			sender.sendMessage(color(msg.toString()));
		}
	}

	/**
	 * Sends a message to everyone in a world
	 * 
	 * @param world World to send message to
	 * @param msg   Message to send
	 */
	public static void tell(World world, Object msg) {
		if (world == null || msg == null)
			return;
		world.getPlayers().forEach((p) -> tell(p, msg));
	}

	/**
	 * Sends a message to all players with a specific permission
	 * 
	 * @param perm Permission to require
	 * @param msg  Message to send
	 */
	public static void tell(String perm, Object msg) {
		Bukkit.getOnlinePlayers().stream().filter((p) -> p.hasPermission(perm)).forEach((p) -> tell(p, msg));
	}

	/**
	 * Announces a message to all players
	 * 
	 * @param msg Message to announce
	 */
	public static void announce(String msg) {
		Bukkit.getOnlinePlayers().forEach((player) -> {
			tell(player, msg);
		});
	}

	/**
	 * Logs a message to console
	 * 
	 * @param msg Message to log
	 * @deprecated
	 */
	public static void log(Object msg) {
		tell(Bukkit.getConsoleSender(), "[" + plugin.getDescription().getName() + "] " + msg);
	}

	/**
	 * Colored boolean
	 * 
	 * @param bool true/false
	 * @return Green True or Red False
	 */
	public static String TorF(boolean bool) {
		return bool ? "&aTrue&r" : "&cFalse&r";
	}

	/**
	 * Returns a text progress bar
	 * 
	 * @param prog   0-total double value of progress
	 * @param total  Max amount that progress bar should represent
	 * @param length Length in chars for progress bar
	 * @return
	 */
	public static String progressBar(double prog, double total, int length) {
		return progressBar("&a\u258D", "&c\u258D", prog, total, length);
	}

	/**
	 * Returns a text progress bar with specified chars
	 * 
	 * @param progChar   Progress string to represent progress
	 * @param incomplete Incomplete string to represent amount left
	 * @param prog       0-total double value of progress
	 * @param total      Max amount that progress bar should represent
	 * @param length     Length in chars for progress bar
	 * @return
	 */
	public static String progressBar(String progChar, String incomplete, double prog, double total, int length) {
		String disp = "";
		double progress = Math.abs(prog / total);
		int len = length;
		for (double i = 0; i < len; i++) {
			if (i / len < progress) {
				disp = disp + progChar;
			} else {
				disp = disp + incomplete;
			}
		}
		return color(disp);
	}

	/**
	 * Returns a string for shortened decimal
	 * 
	 * @param decimal Decimal to shorten
	 * @param length  Amount of characters after the ., will add on 0's to meet
	 *                minimum
	 * @return Input: "5978.154123" (Length of 3) Output: "5978.154"
	 */
	public static String parseDecimal(String decimal, int length) {
		if (decimal.contains(".")) {
			if (decimal.split("\\.").length == 1)
				decimal += "0";
			if (decimal.split("\\.")[1].length() > 2) {
				decimal = decimal.split("\\.")[0] + "."
						+ decimal.split("\\.")[1].substring(0, Math.min(decimal.split("\\.")[1].length(), length));
			}
		} else {
			decimal += ".0";
		}
		while (decimal.split("\\.")[1].length() < length)
			decimal += "0";
		return decimal;
	}

	/**
	 * Returns a string for shortened decimal
	 * 
	 * @param decimal Decimal to shorten
	 * @param length  Amount of characters after the .
	 * @return Input: 5978.154123 (Length of 3) Output: "5978.154"
	 */
	public static String parseDecimal(double decimal, int length) {
		return parseDecimal(decimal + "", length);
	}

	public static String getTime(long mils) {
		boolean isNegative = mils < 0;
		double mil = Math.abs(mils);
		String names[] = { "milliseconds", "seconds", "minutes", "hours", "days", "weeks", "months", "years", "decades",
				"centuries" };
		String sNames[] = { "millisecond", "second", "minute", "hour", "day", "week", "month", "year", "decade",
				"century" };
		Double length[] = { 1.0, 1000.0, 60000.0, 3.6e+6, 8.64e+7, 6.048e+8, 2.628e+9, 3.154e+10, 3.154e+11,
				3.154e+12 };
		String suff = "";
		for (int i = length.length - 1; i >= 0; i--) {
			if (mil >= length[i]) {
				if (suff.equals(""))
					suff = names[i];
				mil = mil / length[i];
				if (mil == 1) {
					suff = sNames[i];
					// suff = suff.substring(0, suff.length() - 1);
				}
				break;
			}
		}
		String name = mil + "";
		if (Math.round(mil) == mil) {
			name = (int) Math.round(mil) + "";
		}
		if (name.contains(".")) {
			if (name.split("\\.")[1].length() > 2)
				name = parseDecimal(name, 2);
		}
		if (isNegative)
			name = "-" + name;
		return name + " " + suff;
	}

	public static double getMills(String msg) {
		String val = "";
		double mills = -1;
		for (char c : msg.toCharArray()) {
			if ((c + "").matches("[0-9\\.-]")) {
				val = val + c;
			} else {
				break;
			}
		}
		try {
			mills = Double.valueOf(val) * 1000;
		} catch (Exception e) {
			return 0.0;
		}

		Double amo[] = { 60.0, 3600.0, 86400.0, 604800.0, 2.628e+6, 3.154e+7, 3.154e+8, 3.154e+9 };
		String[] names = { "m", "h", "d", "w", "mo", "y", "de", "c" };
		for (int i = amo.length - 1; i >= 0; i--) {
			if (msg.toLowerCase().contains(names[i])) {
				mills = mills * amo[i];
				break;
			}
		}

		return mills;
	}

	public static String conjoin(String separator, Collection<?> messages) {
		if (messages == null || messages.isEmpty())
			return "";
		StringBuilder builder = new StringBuilder();
		messages.forEach((str) -> builder.append(str + separator));
		return builder.toString().substring(0, Math.max(0, builder.toString().length() - separator.length()));
	}

	public static String hash(String msg, int length, int iterations) {
		msg = msg.replace(" ", "");
		if (msg.isEmpty())
			return "";
		String result = "", tmp = "";
		double total = 0, avg = 0, odds = 0, evens = 0;
		for (int i = 0; i < msg.length(); i++) {
			total += msg.charAt(i);
			if (i % 2 == 0) {
				odds += msg.charAt(i);
			} else {
				evens += msg.charAt(i);
			}
		}
		avg = total / msg.length();
		result += "" + (msg.length() / 5.58462 * (evens + 6481.5135)) * ((double) msg.charAt(0)) * (double) total / avg
				* odds;

		result += result.length() * Math.pow(evens, odds) / result.charAt((result.length() - 1) / msg.length() / 5) + ""
				+ Math.copySign(odds, evens * total);

		for (int i = 1; i < msg.length() - 1; i += Math.ceil(msg.length() / 5.0)) {
			result = result.substring(0, Math.min(i, result.length())) + Math.pow(avg * total, i)
					+ Math.sqrt(odds) * Math.log(evens) + result.substring(Math.min(i, result.length()));
		}

		for (int i = (int) Math.ceil(result.length() / avg); i < result.length(); i += Math
				.max(Math.ceil(result.length() / (length * Math.PI)), 1)) {
			if ((result.charAt(i) + "").matches("[0-9]")) {
				result = result.substring(0, i - 1)
						+ (char) (Math.floor((Integer.parseInt(result.charAt(i) + "") * 2.8)) + 65)
						+ result.substring(i, result.length() - 1);
			}
		}

		result = result.replaceAll("[^A-Z0-9]", "");

		while (result.length() < length)
			result = hash(result, length - iterations, --iterations);

		while (iterations > 0)
			result = hash(result, length + iterations, --iterations);

		for (int i = 0; i < result.length() && i < length; i++)
			tmp += result.charAt((int) ((i + avg) % result.length()));

		return tmp;
	}

	public static String hashWithSalt(String salt, String password, int length, int iterations) {
		String result = salt + password;
		for (int i = 0; i < iterations; i++)
			result = hash(salt + result, length, 1);
		return result;
	}

	public static String filter(String msg, List<String> swears) {
		String raw = msg; /** plugin.getSwears() is a List of all words that should be filtered */
		for (String word : swears) {
			char[] letters = raw.toCharArray();
			for (int i = 0; i < raw.length() && i < letters.length; i++) {
				String tmp = "";
				String w = "";
				int p = 0;
				while (p + i < letters.length && tmp.length() < word.length()) {
					tmp += (letters[p + i] + "").replaceAll("[^a-zA-Z]", "").toLowerCase();
					w += letters[p + i] + "";
					p++;
				}

				w = w.trim();
				if (tmp.toLowerCase().contains(word.toLowerCase())) {
					String r = "";
					for (int ii = 0; ii < w.trim().length(); ii++)
						r += "*";
					raw = raw.replace(w.trim(), r);
					break;
				}
			}
		}

		Pattern p = Pattern.compile(
				"(.+(.|,|dot|)(com|net|org|me|edu|info)|[0-9]+(.|,|dot|)[0-9]+(.|,|dot|)[0-9]+(.|,|dot|)[0-9]+)");
		Matcher m = p.matcher(raw);

		/**
		 * This part filters any URLs that you do not want. You can change [YOUR URL] to
		 * allow your own server's URL
		 */
		if (m.matches() && !m.group(0).matches("(https:\\/\\/)?(www\\.)?scorchgamez\\.com")) {
			String r = "";
			for (int i = 0; i < m.group(0).length(); i++)
				r += "*";
			raw = raw.replace(m.group(0), r);
		}

		String result = "";

		/**
		 * This filters any non regular ascii characters from the message
		 */
		for (char c : raw.toCharArray()) {
			if (c >= 32 && c <= 127)
				result += c;
		}
		return result;

	}
}
