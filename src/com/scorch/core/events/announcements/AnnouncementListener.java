package com.scorch.core.events.announcements;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.MSG;
import com.scorch.core.utils.Sounds;

public class AnnouncementListener implements Listener {
	public AnnouncementListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onAnnouncement(AnnouncementSendEvent event) {
		if (!event.getServer().equalsIgnoreCase(ScorchCore.getInstance().getServerName())
				&& !event.getServer().equalsIgnoreCase("all"))
			return;

		String msg = event.getMessage(), perm = event.getPermission();

		String title = ScorchCore.getInstance().getMessage("announcement-title").replace("%msg%", msg),
				subtitle = ScorchCore.getInstance().getMessage("announcement-subtitle").replace("%msg%", msg),
				chat = ScorchCore.getInstance().getMessage("announcement-message").replace("%msg%", msg);

		if (event.getPermission() == null) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				MSG.tell(p, chat);
				p.sendTitle(MSG.color(title), MSG.color(subtitle), 0, 100, 0);
				try {
					p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2, 1);
				} catch (Exception expected) {
					p.playSound(p.getLocation(), Sounds.LEVEL_UP.bukkitSound(), 2, 1);
				}
			}
		} else {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (!p.hasPermission(perm))
					continue;
				MSG.tell(p, chat);
				p.sendTitle(MSG.color(title), MSG.color(subtitle), 0, 100, 0);
				try {
					p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2, 1);
				} catch (Exception expected) {
					p.playSound(p.getLocation(), Sounds.LEVEL_UP.bukkitSound(), 2, 1);
				}
			}
		}
	}

}
