package com.scorch.core.events.servers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.communication.exceptions.WebSocketException;
import com.scorch.core.utils.Logger;

public class ServerTrackListener implements Listener {
	public ServerTrackListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
		new BukkitRunnable() {
			@Override
			public void run() {
				ServerRequestListEvent request = new ServerRequestListEvent(ScorchCore.getInstance().getServerName());
				try {
					ScorchCore.getInstance().getCommunicationModule().dispatchEvent(request);
				} catch (WebSocketException e) {
					e.printStackTrace();
				}
				ScorchCore.getInstance().getCommunicationModule().getServers().clear();
			}
		}.runTaskTimer(ScorchCore.getInstance(), 20 * 5, 20 * 60 * 5);
	}

	@EventHandler
	public void serverRequestEvent(ServerRequestListEvent event) {
		Logger.log("Requesting server list...");
		ServerExistsEvent see = new ServerExistsEvent(ScorchCore.getInstance().getServerName());
		try {
			ScorchCore.getInstance().getCommunicationModule().dispatchEvent(see);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void serverExistsEvent(ServerExistsEvent event) {
		Logger.log("Server exists: " + event.getServer());
		ScorchCore.getInstance().getCommunicationModule().addServer(event.getServer());
	}

}
