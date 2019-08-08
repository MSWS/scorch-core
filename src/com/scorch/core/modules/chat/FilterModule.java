package com.scorch.core.modules.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.chat.FilterEntry.FilterType;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.DataPrimaryKeyException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.MSG;

public class FilterModule extends AbstractModule implements Listener {

	private List<FilterEntry> entries, def;

	private Map<FilterType, List<String>> links;

	private Listener invListener;

	public FilterModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		entries = new ArrayList<FilterEntry>();
		def = new ArrayList<>();
		links = new HashMap<FilterType, List<String>>();

		invListener = new FilterInventoryListener();

		for (FilterType type : FilterType.values())
			links.put(type, new ArrayList<>());

		for (String word : new String[] { "anal", "anel", "anil", "arse", "ass", "beotch", "bitc", "bitch", "bith",
				"bobs", "boob", "boobs", "breast", "breasts", "breats", "brest", "btch", "chde", "chod", "chode",
				"chude", "dck", "dic", "dick", "dik", "dk", "erohw", "faag", "fac", "fack", "fahk", "fak", "fck", "feg",
				"fk", "fuc", "fuck", "fuhck", "fuhk", "fuk", "gae", "gai", "gay", "gaye", "genatals", "geneitals",
				"genetels", "genetls", "genital", "genitals", "genitls", "gentals", "gnital", "gnitals", "haipicksel",
				"haipixel", "hctib", "highpixel", "hipi", "hipixel", "hoar", "hore", "hpixel", "hypeexel", "hypi",
				"hypicksel", "hypixel", "jenitals", "kcuf", "peanis", "peenees", "peeneis", "peenis", "peneis", "penes",
				"penis", "phuc", "phuck", "phuk", "phukc", "picksel", "pnis", "poosee", "pooseh", "poosie", "poosies",
				"poosy", "pssie", "pssy", "psusies", "puhsies", "pusie", "pusies", "pussay", "pussee", "pusseh",
				"pussey", "pussie", "pussy", "pusy", "qeeur", "qeuer", "queeer", "queer", "quer", "reetard", "retaard",
				"retard", "retrd", "ritard", "sax", "sex", "sexting", "sexy", "sh1t", "shat", "shiit", "shit", "shite",
				"sht", "siht", "slt", "slut", "thot", "thout", "tihs", "tuls", "vagen", "vagene", "vagina", "vagine",
				"vajene", "vajina", "vajine", "whoar", "whoor", "whor", "whore", "whre", "whure", "kunt", "cunt" })
			def.add(new FilterEntry(word, FilterType.REGULAR));

		def.add(new FilterEntry("nigger", FilterType.MANDATORY));

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					Logger.log("&9Loading swear messages...");
					ScorchCore.getInstance().getDataManager().createTable("swears", FilterEntry.class);
					ScorchCore.getInstance().getDataManager().getAllObjects("swears").forEach(cm -> {
						FilterEntry fe = (FilterEntry) cm;
						entries.add(fe);

						List<String> words = links.getOrDefault(fe.getType(), new ArrayList<>());
						words.add(fe.getWord());
						links.put(fe.getType(), words);
					});

					if (entries.isEmpty())
						for (FilterEntry msg : def.stream().filter(cm -> !containsWord(cm.getWord()))
								.collect(Collectors.toList())) {
							ScorchCore.getInstance().getDataManager().saveObject("swears", msg);
							entries.add(msg);

							List<String> words = links.getOrDefault(msg.getType(), new ArrayList<>());
							words.add(msg.getWord());
							links.put(msg.getType(), words);
						}
					Logger.log("&aSuccessfully loaded &e" + entries.size() + "&a swear word"
							+ (entries.size() == 1 ? "" : "s") + ".");
				} catch (NoDefaultConstructorException | DataObtainException e) {
					e.printStackTrace();
				} catch (DataPrimaryKeyException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
	}

	private boolean containsWord(String word) {
		for (FilterEntry entry : entries) {
			if (entry.getWord().equals(word))
				return true;
		}
		return false;
	}

	@Override
	public void disable() {
		entries.clear();

		InventoryClickEvent.getHandlerList().unregister(invListener);
	}

	public void addWord(FilterEntry entry) {
		entries.add(entry);
		List<String> tmp = links.getOrDefault(entry.getType(), new ArrayList<>());
		tmp.add(entry.getWord());
		links.put(entry.getType(), tmp);

		ScorchCore.getInstance().getDataManager().saveObject("swears", entry);
	}

	public void removeWord(FilterEntry entry) {
		entries.remove(entry);
		links.remove(entry.getType(), entry.getWord());

		ScorchCore.getInstance().getDataManager().deleteObjectAsync("swears", new SQLSelector("word", entry.getWord()),
				new SQLSelector("type", entry.getType()));
	}

	public String filter(String message, FilterType... level) {
		for (FilterType type : level) {
			if (type == FilterType.ALLOW)
				continue;
			message = MSG.filter(message, links.get(type), links.get(FilterType.ALLOW), false);
		}
		return message;
	}

	public boolean testBot(String message) {
		String filtered = MSG.filter(message, links.get(FilterType.ADVERTISING), links.get(FilterType.ALLOW), true);

		return !filtered.equals(message);
	}

	public FilterEntry getFilterEntry(String word) {
		return entries.stream().filter(filter -> filter.getWord().equalsIgnoreCase(word)).findFirst().orElse(null);
	}

	public Inventory getFilterGUI(int page) {
		Inventory hist = Bukkit.createInventory(null, 54, "Swear Words");

		List<FilterEntry> entries = this.entries;
		Collections.sort(entries);

		int slot = 0;

		for (int i = (page * (hist.getSize() - 9)); i < (page * (hist.getSize() - 9)) + hist.getSize() - 9
				&& i < entries.size(); i++) {
//			Punishment p = entries.get(i);
			FilterEntry fe = entries.get(i);
			Material type = Material.GLASS;
			switch (fe.getType()) {
			case ADVERTISING:
				type = Material.BEDROCK;
				break;
			case ALLOW:
				type = Material.GREEN_WOOL;
				break;
			case MANDATORY:
				type = Material.REDSTONE_BLOCK;
				break;
			case REGULAR:
				type = Material.PAPER;
				break;
			}

			ItemStack item = new ItemStack(type, fe.getWord().length());

			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(MSG.color("&r" + fe.getWord()));
			List<String> lore = new ArrayList<>();
			lore.add(MSG.color("&7" + fe.getType()));

			lore.add("");
			lore.add(MSG.color("&e&lQ &7- Remove entry"));
			FilterType next = FilterType.values()[fe.getType().ordinal() - 1 < 0 ? FilterType.values().length - 1
					: fe.getType().ordinal() - 1],
					prev = FilterType.values()[(fe.getType().ordinal() + 1) % FilterType.values().length];

			lore.add(MSG.color("&e&lLeft-Click &7- Set to " + prev));
			lore.add(MSG.color("&e&lRight-Click &7- Set to " + next));
			meta.setLore(lore);
			item.setItemMeta(meta);

			hist.setItem(slot, item);
			slot++;
		}

		if (page > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ItemMeta bMeta = back.getItemMeta();
			bMeta.setDisplayName(MSG.color("&aPrevious Page"));
			back.setItemMeta(bMeta);
			hist.setItem(hist.getSize() - 9, back);
		}
		if (hist.getItem(hist.getSize() - 10) != null) {
			ItemStack next = new ItemStack(Material.ARROW);
			ItemMeta nMeta = next.getItemMeta();
			nMeta.setDisplayName(MSG.color("&aNext Page"));
			next.setItemMeta(nMeta);
			hist.setItem(hist.getSize() - 1, next);
		}
		return hist;
	}

	public List<FilterEntry> getEntries() {
		Collections.sort(entries);
		return entries;
	}

}
