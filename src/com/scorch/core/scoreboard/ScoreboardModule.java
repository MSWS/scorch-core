package com.scorch.core.scoreboard;

import com.scorch.core.modules.AbstractModule;

/**
 * This class is just for internal notes on how to do a scoreboard
 * 
 * @deprecated Does nothing, will not work on the server until updated
 * @author imodm
 *
 */
public class ScoreboardModule extends AbstractModule {

	public ScoreboardModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
	}

	@Override
	public void disable() {
	}
	
	
	/*Scoreboard board;
	ConfigurationSection scoreboard = Main.plugin.config.getConfigurationSection("Scoreboard");
	PlayerManager pManager = new PlayerManager();

	double tick = 0, spd = 1, changed = 0;
	int length = 25;
	List<String> sLines = new ArrayList<String>();
	String name = "", prefix = "", lastColor1 = "", lastColor2 = "";
	Runtime runtime = Runtime.getRuntime();
	Economy eco = Main.plugin.getEcononomy();
	List<String> codes;

	public void refresh() {
		sLines = flip(Main.plugin.config.getStringList("Scoreboard.Lines"));
		length = Main.plugin.config.getInt("Scoreboard.Length");
		prefix = Main.plugin.config.getString("Scoreboard.Prefix");
		spd = Main.plugin.config.getDouble("Scoreboard.Speed");
		codes = new ArrayList<String>();
		for (int i = 0; i < 10; i++)
			codes.add(i + "");
		for (int i = 97; i <= 102; i++)
			codes.add(((char) i) + "");
		lastColor1 = "&" + codes.get((int) Math.floor(Math.random() * codes.size())) + "&l";
		lastColor2 = "&" + codes.get((int) Math.floor(Math.random() * codes.size())) + "&l";

		System.gc();
	}

	public void register() {
		refresh();
		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (pManager.getInfo(player, "scoreboard") != null && !pManager.getBoolean(player, "scoreboard")
							|| pManager.getInfo(player, "login") != null)
						continue;
					name = Main.plugin.config.getString("Scoreboard.Title");
					List<String> lines = new ArrayList<String>();
					board = player.getScoreboard();
					// Anti Lag/Flash Scoreboard functions
					if (board != null && player.getScoreboard().getObjective("basic") != null) {
						if (board.getObjectives().size() > sLines.size())
							continue;
						Objective obj = board.getObjective("basic");
						List<String> oldLines = pManager.getStringList(player, "oldLines");
						for (int i = 0; i < 15 && i < sLines.size() && i < oldLines.size(); i++) {
							String sLine = MSG.parse(player, sLines.get(i)), nLine = "";
//							if (sLine.length() > 40) {
//								nLine = sLine.substring((int) (tick % sLine.length()))
//										+ sLine.substring(0, (int) (tick % sLine.length()));
//								nLine = nLine.substring(0, Math.min(40, nLine.length()));
//							} else {
//								nLine = sLine.substring(0, Math.min(40, sLine.length()));
//							}
							if (sLine.startsWith("scroll")) {
								sLine = sLine.substring("scroll".length());
								nLine = sLine.substring((int) (tick % sLine.length()))
										+ sLine.substring(0, (int) (tick % sLine.length()));
								nLine = nLine.substring(0, Math.min(18, nLine.length()));
							} else {
								nLine = sLine.substring(0, Math.min(40, sLine.length()));
							}

							// sLine = sLine.substring(0, Math.min(sLine.length(), 40));
							lines.add(nLine);
							if (board.getEntries().contains(nLine))
								continue;
							board.resetScores(MSG.parse(player, oldLines.get(i)));
							obj.getScore(nLine).setScore(i + 1);
						}
						name = MSG.parse(player, name);
						String disp = "";
						pManager.setInfo(player, "oldLines", lines);
						if (name.length() > length) {
							// name = name.substring((int) Math.round((tick / 2.0) % name.length()))
							// + name.substring(0, (int) (tick / 2.0) % name.length());
							// name = name.substring(0, Math.min(5, name.length()));
							disp = name.substring((int) (tick % name.length()))
									+ name.substring(0, (int) (tick % name.length()));
							disp = disp.substring(0, Math.min(length, disp.length()));
						} else {
							disp = name;
						}
						// obj.setDisplayName(MSG.color(prefix+disp));
						obj.setDisplayName(MSG.color(animate(disp, tick)));
					} else {
						board = Bukkit.getScoreboardManager().getNewScoreboard();
						Objective obj = board.registerNewObjective("basic", "dummy");
						player.setScoreboard(board);
						obj.setDisplaySlot(DisplaySlot.SIDEBAR);
						int pos = 1;
						for (String res : sLines) {
							String line = MSG.parse(player, res);
							line = line.substring(0, Math.min(40, line.length()));
							obj.getScore(line).setScore(pos);
							lines.add(line);
							if (pos >= 15 || pos >= sLines.size())
								break;
							pos++;
						}
						pManager.setInfo(player, "oldLines", lines);
					}
					if (board.getEntries().size() > sLines.size())
						refresh(player);
				}
				tick += spd;
			}
		}.runTaskTimer(Main.plugin, 0, 1);

	}

//	@SuppressWarnings("deprecation")
//	private String parse(Player player, String entry) {
//		int ping = 0;
//		try {
//			Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
//			ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		String color = "&a";
//		if (ping > 1000) {
//			color = "&4";
//		} else if (ping > 500) {
//			color = "&c";
//		} else if (ping > 300) {
//			color = "&e";
//		}
//		Block target = null;
//		try {
//			target = player.getTargetBlock((Set<Material>) null, 100);
//		} catch (Exception e) {
//		}
//		String result = MSG.color(entry.replace("%world%", player.getWorld().getName())
//				.replace("%time%", Utils.worldTime(player.getWorld().getTime()))
//				.replace("%online%", Bukkit.getOnlinePlayers().size() + "")
//				.replace("%rank%", pManager.getPrefix(player).equals("") ? "Default" : pManager.getPrefix(player))
//				.replace("%x%", Utils.parseDecimal(player.getLocation().getX() + "", 2))
//				.replace("%y%", Utils.parseDecimal(player.getLocation().getY() + "", 2))
//				.replace("%z%", Utils.parseDecimal(player.getLocation().getZ() + "", 2))
//				.replace("%clientground%", MSG.TorF(player.isOnGround()))
//				.replace("%serverground%", MSG.TorF(player.getLocation().getY() % .5 == 0))
//				.replace("%totalmemory%", runtime.totalMemory() / 1048576L + "")
//				.replace("%freememory%", runtime.freeMemory() / 1048576L + "")
//				.replace("%usedmemory%", (runtime.totalMemory() - runtime.freeMemory()) / 1048576L + "")
//				.replace("%memory%",
//						Utils.parseDecimal((((double) runtime.totalMemory() - (double) runtime.freeMemory())
//								/ (double) runtime.totalMemory()) * 100.0 + "", 2))
//				.replace("%ping%", color + ping).replace("%targetblock%", MSG.camelCase(target.getType().toString()))
//				.replace("%uuid%", player.getUniqueId() + "").replace("%flying%", MSG.TorF(player.isFlying()))
//				.replace("%pitch%", Utils.parseDecimal(player.getLocation().getPitch() + "", 2))
//				.replace("%yaw%", Utils.parseDecimal(player.getLocation().getYaw() + "", 2))
//				.replace("%vanish%", MSG.TorF(pManager.isVanished(player)))
//				.replace("%rndcolor%", "&"+codes.get((int) Math.floor(Math.random() * codes.size()))));
//		if (!player.getName().equals(player.getDisplayName())) {
//			result = result.replace("%player%", player.getName() + " &3(&b" + player.getDisplayName() + "&3)");
//		} else {
//			result = result.replace("%player%", player.getDisplayName());
//		}
//		if (eco != null) {
//			result = result.replace("%balance%", Utils.parseDecimal(eco.getBalance(player) + "", 2));
//		} else {
//			result = result.replace("%balance%", "0");
//
//		}
//
//		if (pManager.getInfo(player, "lastJoin") != null) {
//			result = result.replace("%playtime%", TimeManager.getTime(pManager.getDouble(player, "playtime")
//					+ (System.currentTimeMillis() - pManager.getDouble(player, "lastJoin"))));
//		}
//
//		if (result == null || Bukkit.getPluginManager().getPlugin("Factions") == null)
//			return MSG.color(result);
//
//		if (Bukkit.getPluginManager().isPluginEnabled("Factions")) {
//			MPlayer mp = MPlayer.get(player);
//			Faction f = BoardColl.get().getFactionAt(PS.valueOf(player.getLocation()));
//			result = result.replace("%claimed%", f.getName());
//			if (mp.hasFaction()) {
//				result = result.replace("%faction%", mp.getFactionName())
//						.replace("%power%", Utils.parseDecimal(mp.getFaction().getPower() + "", 2))
//						.replace("%maxpower%", Utils.parseDecimal(mp.getFaction().getPowerMax() + "", 2))
//						.replace("%factiononline%", mp.getFaction().getOnlinePlayers().size()+"");
//			} else {
//				result = result.replace("%faction%", "None").replace("%power%", "0").replace("%maxpower%", "0").replace("%factiononline%", "0");
//			}
//		} else {
//			result = result.replace("%faction%", "None").replace("%power%", "0").replace("%maxpower%", "0");
//		}
//		// return MSG.color(result.substring(0, Math.min(result.length(), 40)));
//		return MSG.color(result);
//	}

	public String animate(String line, double time) {
		int pos = (int) Math.floor((time * 1) % line.length());
		String name = line;
		String c1 = lastColor1, c2 = lastColor2;
		if (pos == line.length() - 1 && System.currentTimeMillis() - changed > 100) {
			// name = "&c"+line.substring(0, line.length()-pos) + "&a"+
			// line.substring(line.length()-pos);
			// name = line.substring(0, line.length()-1-pos) + color+
			// line.substring(line.length()-1-pos);
			List<String> codes = new ArrayList<String>();
			for (int i = 0; i < 10; i++)
				codes.add(i + "");
			for (int i = 97; i <= 102; i++)
				codes.add(((char) i) + "");
			c2 = c1;
			c1 = "&" + codes.get((int) Math.floor(Math.random() * codes.size())) + "&l";
			double rnd = new Random().nextDouble();
			if (rnd > .97) {
				c1 = c1 + "&m";
			} else if (rnd > .94) {
				c1 = c1 + "&n";
			} else if (rnd > .91) {
				c1 = c1 + "&k";
			} else if (rnd > .88) {
				c1 = c1 + "&o";
			}
			lastColor1 = c1;
			lastColor2 = c2;
			changed = System.currentTimeMillis();
		} else {
			name = c1 + line.substring(0, pos) + c2 + line.substring(pos);
		}
		if (pos == line.length() - 1)
			name = c2 + line;
		return name;
	}

	private List<String> flip(List<String> array) {
		List<String> result = new ArrayList<String>();
		for (int i = array.size() - 1; i >= 0; i--) {
			result.add(array.get(i));
		}
		return result;
	}

	private void refresh(Player player) {
		for (String res : board.getEntries()) {
			boolean keep = false;
			for (String line : sLines) {
				if (MSG.parse(player, res).equals(MSG.parse(player, line)))
					keep = true;
			}
			if (!keep)
				board.resetScores(res);
		}
	}
	*/
}