package com.scorch.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Author kitsune#0001
 * Written in 2017
 * Last revision June 2019
 */

public class GUILib {
	
	static HashMap<UUID, GUIWindow> guiWindows;
	static GUIListener guiListener;
	
	public static void initialise (Plugin plugin) {
		GUILib.guiWindows = new HashMap<UUID, GUIWindow>();
		GUILib.guiListener = new GUIListener(plugin);
	}
	
	public static GUIWindow createGUI (String guiName, int rows, GUIItem... items) {
		GUIWindow gui = new GUIWindow(guiName, rows, items);
		GUILib.guiWindows.put(gui.getId(), gui);
		return gui;
	}
	
	public static GUIWindow getGUIWindow (Inventory inv) {
		for(GUIWindow gui : GUILib.guiWindows.values()) {
			if(gui.getGui().equals(inv)) {
				return gui;
			}
		}
		return null;
	}
	
	public static interface GUIClickHandler {

		public void onClick(InventoryClickEvent e);
		
	}
	
	public static class GUIWindow {

		private UUID id;
		private String name;
		private int slots;
		
		private List<GUIItem> items;
		
		private Inventory gui;
		
		private GUIWindow (String guiName, int rows, GUIItem... items) {

			if(items == null) {
				items = new GUIItem[]{};
			}

			this.name = guiName;
			this.slots = rows*9;
			this.id = UUID.randomUUID();
			this.items = Arrays.asList(items);
			
			this.gui = Bukkit.createInventory(null, slots, guiName);
			for(GUIItem item : this.items) {
				if(item.getSlot() > this.slots) {
					try {
						throw new Exception("Tried to add a GUIItem to an invalid slot.");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				this.gui.setItem(item.getSlot(), item.getItemStack());
			}
		}
		
		public void open (Player p) {
			p.openInventory(this.getGui());
		}
		
		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<GUIItem> getItems() {
			return items;
		}

		public void setItems(List<GUIItem> items) {
			this.items = items;
		}

		public int getSlots() {
			return slots;
		}

		public void setSlots(int slots) {
			this.slots = slots;
		}

		public Inventory getGui() {
			return gui;
		}

		public void setGui(Inventory gui) {
			this.gui = gui;
		}
		
	}
	
	public static class GUIItem {

		private ItemStack itemStack;
		private int slot;
		private GUIClickHandler clickHandler;
		
		public GUIItem (ItemStack itemStack, int slot, GUIClickHandler clickHandler) {
			this.itemStack = itemStack;
			this.slot = slot;
			this.clickHandler = clickHandler;
		}

		public ItemStack getItemStack() {
			return itemStack;
		}

		public void setItemStack(ItemStack itemStack) {
			this.itemStack = itemStack;
		}

		public GUIClickHandler getClickHandler() {
			return clickHandler;
		}

		public void setClickHandler(GUIClickHandler clickHandler) {
			this.clickHandler = clickHandler;
		}

		public int getSlot() {
			return slot;
		}

		public void setSlot(int slot) {
			this.slot = slot;
		}
		
	}
	
	public static class GUIListener implements Listener{

		
		public GUIListener (Plugin plugin) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}

		@EventHandler
		public void onInventoryClose (InventoryCloseEvent e) {
			if(e.getInventory() != null && GUILib.getGUIWindow(e.getInventory()) != null) {
				GUILib.guiWindows.remove(GUILib.getGUIWindow(e.getInventory()).getId());
			}
		}
		
		@EventHandler
		public void onInventoryClick (InventoryClickEvent e) {
			Inventory inv = e.getInventory();
			ItemStack is = e.getCurrentItem();
			if(inv != null && GUILib.getGUIWindow(inv) != null) {
				e.setCancelled(true);
				GUIWindow gui = GUILib.getGUIWindow(e.getClickedInventory());
				for(GUIItem item : gui.getItems()) {
					if(is != null && item.getItemStack().equals(is)) {
						item.getClickHandler().onClick(e);
					}
				}
			}
		}	
	}
}
