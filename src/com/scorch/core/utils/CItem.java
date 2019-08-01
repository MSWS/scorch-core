package com.scorch.core.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CItem {

	private ItemStack item;

	private String name;
	private List<String> lore;

	public CItem(Material mat) {
		this.item = new ItemStack(mat);
	}

	public CItem name(String name) {
		this.name = MSG.color(name);
		return this;
	}

	public CItem lore(List<String> lore) {
		this.lore = lore.stream().map(s -> MSG.color("&r" + s)).collect(Collectors.toList());
		return this;
	}

	public CItem lore(String... lore) {
		lore(Arrays.asList(lore));
		return this;
	}

	public ItemStack build() {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

}
