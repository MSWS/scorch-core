package com.scorch.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.players.CPlayer;

public class Utils {
	/**
	 * Returns a ranking of all the armor from value
	 * 
	 * @param mat Material to compare
	 * @return Diamond: 4 Iron: 3 Chain: 2 Gold: 1 Leather: 0 Default: 0
	 */
	public static int getArmorValue(Material mat) {
		switch (getArmorType(mat).toLowerCase()) {
		case "diamond":
			return 4;
		case "iron":
			return 3;
		case "chainmail":
			return 2;
		case "gold":
			return 1;
		case "leather":
			return 0;
		default:
			return 0;
		}
	}

	/**
	 * Gets the armor slot that a type of armor should be in
	 * 
	 * @param type Material type (DIAMOND_CHESTPLATE, IRON_LEGGINGS, etc)
	 * @return Armor slot Helmet: 3 Chestplate: 2 Leggings: 1 Boots: 0
	 */
	public static int getSlot(Material type) {
		if (!type.name().contains("_"))
			return 0;
		switch (type.name().split("_")[1]) {
		case "HELMET":
			return 3;
		case "CHESTPLATE":
			return 2;
		case "LEGGINGS":
			return 1;
		case "BOOTS":
			return 0;
		}
		return 0;
	}

	/**
	 * Returns type of armor
	 * 
	 * @param mat Material to get type of
	 * @return DIAMOND, IRON, GOLD, CHAINMAIL
	 */
	public static String getArmorType(Material mat) {
		if (!mat.name().contains("_")) {
			return "";
		}
		String name = mat.name().split("_")[0];
		return name;
	}

	/**
	 * Returns if the specified material is armor
	 * 
	 * @param mat Material to check
	 * @return True if armor, false otherwise
	 */
	public static boolean isArmor(Material mat) {
		return mat.name().contains("CHESTPLATE") || mat.name().contains("LEGGINGS") || mat.name().contains("HELMET")
				|| mat.name().contains("BOOTS");
	}

	/**
	 * Gets a block based on the blockface
	 * 
	 * @param block Block to compare face to
	 * @param face  Relative face to get block
	 * @return
	 */
	public static Block blockFromFace(Block block, BlockFace face) {
		int x = 0, y = 0, z = 0;
		if (face == BlockFace.EAST)
			x = 1;
		if (face == BlockFace.WEST)
			x = -1;
		if (face == BlockFace.NORTH)
			z = -1;
		if (face == BlockFace.SOUTH)
			z = 1;
		if (face == BlockFace.UP)
			y = 1;
		if (face == BlockFace.DOWN)
			y = -1;
		return block.getLocation().add(x, y, z).getBlock();
	}

	/**
	 * Returns parsed Inventory from YAML config (guis.yml)
	 * 
	 * @param player Player to parse information with (%player% and other
	 *               placeholders)
	 * @param id     Name of the inventory to parse
	 * @param page   Page of the inventory
	 * @return
	 */
	public static Inventory getGui(OfflinePlayer player, ConfigurationSection section, String id, int page) {
		if (!section.contains(id))
			return null;
		ConfigurationSection gui = section.getConfigurationSection(id);
		if (!gui.contains("Size") || !gui.contains("Title"))
			return null;
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		String title = parseTemp(cp, gui.getString("Title"));
		if (player.isOnline())
			title = title.replace("%world%", player.getPlayer().getWorld().getName());
		title = title.replace("%world%", "");
		Inventory inv = Bukkit.createInventory(null, gui.getInt("Size"), MSG.color(title));
		ItemStack bg = null;
		boolean empty = true;
		for (String res : gui.getKeys(false)) {
			if (!gui.contains(res + ".Icon"))
				continue;
			empty = false;
			if (gui.contains(res + ".Page")) {
				if (page != gui.getInt(res + ".Page"))
					continue;
			} else if (page != 0)
				continue;
			if (player.isOnline()) {
				if (gui.contains(res + ".Permission")
						&& !((Player) player).hasPermission(gui.getString(res + ".Permission"))) {
					continue;
				}
			}
			ItemStack item = parseItem(section, id + "." + res, player);
			if (res.equals("BACKGROUND_ITEM")) {
				bg = item;
				continue;
			}
			int slot = 0;
			if (!gui.contains(res + ".Slot")) {
				while (inv.getItem(slot) != null)
					slot++;
				inv.setItem(slot, item);
			} else {
				inv.setItem(gui.getInt(res + ".Slot"), item);
			}
		}
		if (empty)
			return null;
		if (bg != null) {
			for (int i = 0; i < inv.getSize(); i++) {
				if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
					inv.setItem(i, bg);
				}
			}
		}
		return inv;
	}

	/**
	 * Parses and returns an item from the specified YAML Path Supports
	 * enchantments, damage values, amounts, skulls, lores, and unbreakable
	 * 
	 * @param section Section to get item from
	 * @param path    Specified path after section
	 * @param player  Player to parse the items with (for %player% and other
	 *                placeholders)
	 * @return Parsed ItemStack
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack parseItem(ConfigurationSection section, String path, OfflinePlayer player) {
		ConfigurationSection gui = section.getConfigurationSection(path);
		ItemStack item = new ItemStack(Material.valueOf(gui.getString("Icon")));
		CPlayer cp = ScorchCore.getInstance().getPlayer(player);
		List<String> lore = new ArrayList<String>();
		if (gui.contains("Amount"))
			item.setAmount(gui.getInt("Amount"));
//		if (gui.contains("Data"))
//			item.setDurability((short) gui.getInt("Data"));
		if (gui.contains("Data")) {
			if (Bukkit.getVersion().contains("1.8")) {
				item.setDurability((short) gui.getInt("Data"));
			} else {
				Damageable dmg = (Damageable) item.getItemMeta();
				dmg.setDamage(gui.getInt("Data"));
				item.setItemMeta((ItemMeta) dmg);
			}

		}

		if (gui.contains("Owner")) {
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			if (Bukkit.getVersion().contains("1.8")) {
				meta.setOwner(parseTemp(cp, gui.getString("Owner")));
			} else {
				meta.setOwningPlayer(Bukkit.getOfflinePlayer(parseTemp(cp, gui.getString("Owner"))));
			}
			item.setItemMeta(meta);
		}
		ItemMeta meta = item.getItemMeta();

		if (gui.contains("Name"))
			meta.setDisplayName(MSG.color("&r" + parseTemp(cp, gui.getString("Name"))));
		if (gui.contains("Lore")) {
			for (String temp : gui.getStringList("Lore"))
				lore.add(parseTemp(cp, MSG.color("&r" + temp)));
		}
		if (gui.getBoolean("Unbreakable")) {
//			meta.spigot().setUnbreakable(true);
			meta.setUnbreakable(true);
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		if (gui.contains("Enchantments")) {
			ConfigurationSection enchs = gui.getConfigurationSection("Enchantments");
			for (String enchant : enchs.getKeys(false)) {
				int level = 1;
				if (enchs.contains(enchant + ".Level"))
					level = enchs.getInt(enchant + ".Level");
				if (enchs.contains(enchant + ".Visible") && !enchs.getBoolean(enchant + ".Visible"))
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				item.setItemMeta(meta);
				item.addUnsafeEnchantment(Enchantment.getByName(enchant.toUpperCase()), level);
				meta = item.getItemMeta();
			}
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static String parseTemp(CPlayer cp, String string) {
		for (Entry<String, Object> entry : cp.getTempMap().entrySet()) {
			if (entry.getKey().equals("punishing")) {
				string = string.replace("%punishing%", cp.getTempString("punishing").split("\\|")[1]);
				continue;
			}
			string = string.replace("%" + entry.getKey() + "%", entry.getValue() + "");
		}
		return string;
	}

	/**
	 * Calculates a player's total exp based on level and progress to next.
	 * 
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 * @param player the Player
	 * 
	 * @return the amount of exp the Player has
	 */
	public static int getExp(Player player) {
		return getExpFromLevel(player.getLevel()) + Math.round(getExpToNext(player.getLevel()) * player.getExp());
	}

	/**
	 * Calculates total experience based on level.
	 * 
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 *      "One can determine how much experience has been collected to reach a
	 *      level using the equations:
	 * 
	 *      Total Experience = [Level]2 + 6[Level] (at levels 0-15) 2.5[Level]2 -
	 *      40.5[Level] + 360 (at levels 16-30) 4.5[Level]2 - 162.5[Level] + 2220
	 *      (at level 31+)"
	 * 
	 * @param level the level
	 * 
	 * @return the total experience calculated
	 */
	public static int getExpFromLevel(int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		}
		if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return level * level + 6 * level;
	}

	/**
	 * Calculates level based on total experience.
	 * 
	 * @param exp the total experience
	 * 
	 * @return the level calculated
	 */
	public static double getLevelFromExp(long exp) {
		if (exp > 1395) {
			return (Math.sqrt(72 * exp - 54215) + 325) / 18;
		}
		if (exp > 315) {
			return Math.sqrt(40 * exp - 7839) / 10 + 8.1;
		}
		if (exp > 0) {
			return Math.sqrt(exp + 9) - 3;
		}
		return 0;
	}

	/**
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 *      "The formulas for figuring out how many experience orbs you need to get
	 *      to the next level are as follows: Experience Required = 2[Current Level]
	 *      + 7 (at levels 0-15) 5[Current Level] - 38 (at levels 16-30) 9[Current
	 *      Level] - 158 (at level 31+)"
	 */
	private static int getExpToNext(int level) {
		if (level > 30) {
			return 9 * level - 158;
		}
		if (level > 15) {
			return 5 * level - 38;
		}
		return 2 * level + 7;
	}

	/**
	 * Change a Player's exp.
	 * <p>
	 * This method should be used in place of {@link Player#giveExp(int)}, which
	 * does not properly account for different levels requiring different amounts of
	 * experience.
	 * 
	 * @param player the Player affected
	 * @param exp    the amount of experience to add or remove
	 */
	public static void changeExp(Player player, int exp) {
		exp += getExp(player);

		if (exp < 0) {
			exp = 0;
		}

		double levelAndExp = getLevelFromExp(exp);

		int level = (int) levelAndExp;
		player.setLevel(level);
		player.setExp((float) (levelAndExp - level));
	}

	/**
	 * Used to compare plugin versions Supports any amount of decimals, however no
	 * letters/symbols
	 * 
	 * if oldVer is < newVer, both versions can only have numbers and .'s Outputs:
	 * 5.5, 10.3 | true 2.3.1, 3.1.4.6 | true 1.2, 1.1 | false
	 **/
	public static boolean outdated(String oldVer, String newVer) {
		oldVer = oldVer.replace(".", "");
		newVer = newVer.replace(".", "");
		Double oldV = null, newV = null;
		try {
			oldV = Double.parseDouble(oldVer);
			newV = Double.parseDouble(newVer);
		} catch (Exception e) {
//			MSG.log("&cError! &7Versions incompatible.");
			Logger.warn("&cError! &7Versions incompatible.");
			return false;
		}
		if (oldVer.length() > newVer.length()) {
			newV = newV * (10 * (oldVer.length() - newVer.length()));
		} else if (oldVer.length() < newVer.length()) {
			oldV = oldV * (10 * (newVer.length() - oldVer.length()));
		}
		return oldV < newV;
	}

	/**
	 * Returns the bukkit name of an enchantment
	 * 
	 * @param name "Nickname" of enchant (sharpness, infinity, power, etc.)
	 * @return String og Enchantment Enum
	 */
	public static String getEnchant(String name) {
		switch (name.toLowerCase().replace("_", "")) {
		case "power":
			return "ARROW_DAMAGE";
		case "flame":
			return "ARROW_FIRE";
		case "infinity":
		case "infinite":
			return "ARROW_INFINITE";
		case "punch":
		case "arrowkb":
			return "ARROW_KNOCKBACK";
		case "sharpness":
			return "DAMAGE_ALL";
		case "arthropods":
		case "spiderdamage":
		case "baneofarthropods":
			return "DAMAGE_ARTHORPODS";
		case "smite":
			return "DAMAGE_UNDEAD";
		case "depthstrider":
		case "waterwalk":
			return "DEPTH_STRIDER";
		case "efficiency":
			return "DIG_SPEED";
		case "unbreaking":
			return "DURABILITY";
		case "fireaspect":
		case "fire":
			return "FIRE_ASPECT";
		case "knockback":
		case "kb":
			return "KNOCKBACK";
		case "fortune":
			return "LOOT_BONUS_BLOCKS";
		case "looting":
			return "LOOT_BONUS_MOBS";
		case "luck":
			return "LUCK";
		case "lure":
			return "LURE";
		case "waterbreathing":
		case "respiration":
			return "OXYGEN";
		case "prot":
		case "protection":
			return "PROTECTION_ENVIRONMENTAL";
		case "blastprot":
		case "blastprotection":
			return "PROTECTION_EXPLOSIONS";
		case "feather":
		case "featherfalling":
			return "PROTECTION_FALL";
		case "fireprot":
		case "fireprotection":
			return "PROTECTION_FIRE";
		case "projectileprot":
		case "projectileprotection":
		case "projprot":
			return "PROTECTION_PROJECTILE";
		case "silktouch":
		case "silk":
			return "SILK_TOUCH";
		case "thorns":
			return "THORNS";
		case "aquaaffinity":
		case "aqua":
		case "waterworker":
			return "WATER_WORKER";
		}
		return name.toUpperCase();
	}

	/**
	 * Get the online plugin version from SpigotMC.org
	 * 
	 * @param id ID of the online resource
	 * @return Version
	 */
	public static String getSpigotVersion(int id) {
		try {
			HttpsURLConnection con = (HttpsURLConnection) new URL(
					"https://api.spigotmc.org/legacy/update.php?resource=" + id).openConnection();
			try (BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				return buffer.readLine();
			} catch (Exception ex) {
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 
	 * @param data Wool dye value
	 * @return ChatColor (&a, &b, etc.) for the matching data
	 */
	public static String colorByWoolData(short data) {
		switch (data) {
		case 12:
			return "&b";
		case 11:
			return "&e";
		case 10:
			return "&a";
		case 9:
			return "&d";
		case 8:
			return "&8";
		case 7:
			return "&7";
		case 6:
			return "&3";
		case 5:
			return "&5";
		case 4:
			return "&9";
		case 3:
			return "&6";
		case 2:
			return "&2";
		case 1:
			return "&4";
		case 0:
			return "&0";
		case 15:
			return "&f";
		case 14:
			return "&6";
		case 13:
			return "&d";
		}
		return "";
	}

	/**
	 * Delete a world file from the worlds (World should be unloaded first)
	 * 
	 * @param path File to delete
	 * @return If the world was successfully deleted
	 */
	public static boolean deleteWorld(File path) {
		if (path.exists()) {
			File files[] = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteWorld(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	/**
	 * Returns a list of all unloaded worlds
	 * 
	 * @param includeLoaded Whether or not to include loaded worlds
	 * @return List of the world names
	 */
	public static List<String> getUnloadedWorlds(boolean includeLoaded) {
		List<String> worlds = new ArrayList<>();
		if (includeLoaded) {
			for (World world : Bukkit.getWorlds())
				worlds.add(world.getName());
		}

		for (String res : Bukkit.getWorldContainer().list()) {
			File file = new File(Bukkit.getWorldContainer().toPath() + File.separator + res);
			if (isWorldFile(file) && !worlds.contains(file.getName()))
				worlds.add(file.getName());
		}
		return worlds;
	}

	/**
	 * Returns whether or not a file is a world file
	 * 
	 * @param file Path to check
	 * @return True/False
	 */
	public static boolean isWorldFile(File file) {
		if (file != null && file.list() != null)
			for (String r : file.list())
				if (r.equals("session.lock"))
					return true;
		return false;
	}

	public static EntityType getEntityFromSpawn(Material mat) throws IllegalArgumentException {
		if (!isSpawnEgg(mat))
			throw new IllegalArgumentException(mat + " is not spawn egg");

		try {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < mat.toString().split("_").length - 2; i++) {
				builder.append(mat.toString().split("_")[i] + "_");
			}
			return EntityType.valueOf(builder.toString().substring(0, builder.toString().length() - 1));
		} catch (IllegalArgumentException | NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Sounds getBreakSound(Material mat) {
		switch (mat) {
		case GRASS:
		case GRASS_BLOCK:
		case TALL_GRASS:
		case TNT:
		case ACACIA_LEAVES:
		case BIRCH_LEAVES:
		case DARK_OAK_LEAVES:
		case JUNGLE_LEAVES:
		case OAK_LEAVES:
		case SPRUCE_LEAVES:
			return Sounds.DIG_GRASS;
		case STONE:
		case ANDESITE:
		case GRANITE:
		case ANDESITE_SLAB:
		case GRANITE_SLAB:
		case ANDESITE_STAIRS:
		case GRANITE_STAIRS:
		case ANDESITE_WALL:
		case GRANITE_WALL:
		case COAL_ORE:
		case DIAMOND_ORE:
		case EMERALD_ORE:
		case GOLD_ORE:
		case IRON_ORE:
		case LAPIS_ORE:
		case IRON_BLOCK:
		case GOLD_BLOCK:
		case DIAMOND_BLOCK:
		case REDSTONE_BLOCK:
		case LAPIS_BLOCK:
		case COAL_BLOCK:
			return Sounds.DIG_STONE;
		case GRAVEL:
			return Sounds.DIG_GRAVEL;
		case SAND:
		case RED_SAND:
			return Sounds.DIG_SAND;
		case SNOW:
		case SNOW_BLOCK:
			return Sounds.DIG_SNOW;
		case ACACIA_LOG:
		case BIRCH_LOG:
		case DARK_OAK_LOG:
		case JUNGLE_LOG:
		case OAK_LOG:
		case SPRUCE_LOG:
		case STRIPPED_ACACIA_LOG:
		case STRIPPED_BIRCH_LOG:
		case STRIPPED_DARK_OAK_LOG:
		case STRIPPED_JUNGLE_LOG:
		case STRIPPED_OAK_LOG:
		case STRIPPED_SPRUCE_LOG:
		case ACACIA_PLANKS:
		case BIRCH_PLANKS:
		case DARK_OAK_PLANKS:
		case JUNGLE_PLANKS:
		case OAK_PLANKS:
		case SPRUCE_PLANKS:
			return Sounds.DIG_WOOD;
		case BLACK_WOOL:
		case BLUE_WOOL:
		case BROWN_WOOL:
		case CYAN_WOOL:
		case GRAY_WOOL:
		case GREEN_WOOL:
		case LIGHT_BLUE_WOOL:
		case LIGHT_GRAY_WOOL:
		case LIME_WOOL:
		case MAGENTA_WOOL:
		case ORANGE_WOOL:
		case PINK_WOOL:
		case PURPLE_WOOL:
		case RED_WOOL:
		case WHITE_WOOL:
		case YELLOW_WOOL:
			return Sounds.DIG_WOOL;
		case GLASS:
		case GLASS_PANE:
		case BLACK_STAINED_GLASS:
		case BLACK_STAINED_GLASS_PANE:
		case BLUE_STAINED_GLASS:
		case BLUE_STAINED_GLASS_PANE:
		case BROWN_STAINED_GLASS:
		case BROWN_STAINED_GLASS_PANE:
		case CYAN_STAINED_GLASS:
		case CYAN_STAINED_GLASS_PANE:
		case GRAY_STAINED_GLASS:
		case GRAY_STAINED_GLASS_PANE:
		case GREEN_STAINED_GLASS:
		case GREEN_STAINED_GLASS_PANE:
		case LIGHT_BLUE_STAINED_GLASS:
		case LIGHT_BLUE_STAINED_GLASS_PANE:
		case LIGHT_GRAY_STAINED_GLASS:
		case LIGHT_GRAY_STAINED_GLASS_PANE:
		case LIME_STAINED_GLASS:
		case LIME_STAINED_GLASS_PANE:
		case MAGENTA_STAINED_GLASS:
		case MAGENTA_STAINED_GLASS_PANE:
		case ORANGE_STAINED_GLASS:
		case ORANGE_STAINED_GLASS_PANE:
		case PINK_STAINED_GLASS:
		case PINK_STAINED_GLASS_PANE:
		case PURPLE_STAINED_GLASS:
		case PURPLE_STAINED_GLASS_PANE:
		case RED_STAINED_GLASS:
		case RED_STAINED_GLASS_PANE:
		case WHITE_STAINED_GLASS:
		case WHITE_STAINED_GLASS_PANE:
		case YELLOW_STAINED_GLASS:
		case YELLOW_STAINED_GLASS_PANE:
			return Sounds.GLASS;
		default:
			return Sounds.DIG_STONE;
		}
	}

	public static boolean isSpawnEgg(Material mat) {
		switch (mat) {
		case BAT_SPAWN_EGG:
		case BLAZE_SPAWN_EGG:
		case CAT_SPAWN_EGG:
		case CAVE_SPIDER_SPAWN_EGG:
		case CHICKEN_SPAWN_EGG:
		case COD_SPAWN_EGG:
		case COW_SPAWN_EGG:
		case CREEPER_SPAWN_EGG:
		case DOLPHIN_SPAWN_EGG:
		case DONKEY_SPAWN_EGG:
		case DROWNED_SPAWN_EGG:
		case ELDER_GUARDIAN_SPAWN_EGG:
		case ENDERMAN_SPAWN_EGG:
		case ENDERMITE_SPAWN_EGG:
		case EVOKER_SPAWN_EGG:
		case FOX_SPAWN_EGG:
		case GHAST_SPAWN_EGG:
		case GUARDIAN_SPAWN_EGG:
		case HORSE_SPAWN_EGG:
		case HUSK_SPAWN_EGG:
		case LLAMA_SPAWN_EGG:
		case MAGMA_CUBE_SPAWN_EGG:
		case MOOSHROOM_SPAWN_EGG:
		case MULE_SPAWN_EGG:
		case OCELOT_SPAWN_EGG:
		case PANDA_SPAWN_EGG:
		case PARROT_SPAWN_EGG:
		case PHANTOM_SPAWN_EGG:
		case PIG_SPAWN_EGG:
		case PILLAGER_SPAWN_EGG:
		case POLAR_BEAR_SPAWN_EGG:
		case PUFFERFISH_SPAWN_EGG:
		case RABBIT_SPAWN_EGG:
		case RAVAGER_SPAWN_EGG:
		case SALMON_SPAWN_EGG:
		case SHEEP_SPAWN_EGG:
		case SHULKER_SPAWN_EGG:
		case SILVERFISH_SPAWN_EGG:
		case SKELETON_HORSE_SPAWN_EGG:
		case SKELETON_SPAWN_EGG:
		case SLIME_SPAWN_EGG:
		case SPIDER_SPAWN_EGG:
		case SQUID_SPAWN_EGG:
		case STRAY_SPAWN_EGG:
		case TRADER_LLAMA_SPAWN_EGG:
		case TROPICAL_FISH_SPAWN_EGG:
		case TURTLE_SPAWN_EGG:
		case VEX_SPAWN_EGG:
		case VILLAGER_SPAWN_EGG:
		case VINDICATOR_SPAWN_EGG:
		case WANDERING_TRADER_SPAWN_EGG:
		case WITCH_SPAWN_EGG:
		case WITHER_SKELETON_SPAWN_EGG:
		case WOLF_SPAWN_EGG:
		case ZOMBIE_HORSE_SPAWN_EGG:
		case ZOMBIE_PIGMAN_SPAWN_EGG:
		case ZOMBIE_SPAWN_EGG:
		case ZOMBIE_VILLAGER_SPAWN_EGG:
			return true;
		default:
			return false;
		}
	}
}
