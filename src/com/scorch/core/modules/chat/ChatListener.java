package com.scorch.core.modules.chat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.chat.FilterModule.FilterType;
import com.scorch.core.modules.messages.CMessage;
import com.scorch.core.modules.messages.MessagesModule;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.modules.punish.PunishType;
import com.scorch.core.modules.punish.Punishment;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.MSG;

public class ChatListener implements Listener {
	public ChatListener() {
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler // TODO
	public void onChat(AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		Player player = event.getPlayer();
		
		List<Punishment> punishments = ScorchCore.getInstance().getPunishModule().getPunishments(player.getUniqueId());

		punishments = punishments.stream().filter(Punishment::isActive)
				.filter(p -> p.getType().toString().toLowerCase().contains("mute")).collect(Collectors.toList());

		if (!punishments.isEmpty()) {
			Collections.sort(punishments);

			Punishment p = punishments.get(0);
			MessagesModule messages = ScorchCore.getInstance().getMessages();

			Map<String, String> place = new HashMap<>();
			place.put("%staff%", p.getStaffName());
			place.put("%duration%", MSG.getTime(p.getDuration()));
			place.put("%reason%", p.getReason());
			place.put("%timeleft%", MSG.getTime(p.getDate() + p.getDuration() - System.currentTimeMillis()));
			place.put("%appeal%", messages.getMessage("appeallink").getMessage());

			CMessage msg = p.getDuration() == -1 ? messages.getMessage("permmutemessage")
					: messages.getMessage("tempmutemessage");

			msg.applyPlaceholders(place);

			MSG.tell(player, msg.getMessage());
			return;
		}

		Logger.log(player.getName() + ": " + event.getMessage());
		FilterModule fm = (FilterModule) ScorchCore.getInstance().getModule("FilterModule");

		if (fm.testBot(event.getMessage())) {
			Punishment punishment = new Punishment(player.getUniqueId(), "MSWS", "Bot Spam", System.currentTimeMillis(),
					-1, PunishType.PERM_BAN);
			punishment.setInfo("Message: " + event.getMessage());

			new BukkitRunnable() {

				@Override
				public void run() {
					ScorchCore.getInstance().getPunishModule().addPunishment(punishment);
				}
			}.runTask(ScorchCore.getInstance());
			return;
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			ScorchPlayer sp = ScorchCore.getInstance().getDataManager().getScorchPlayer(p.getUniqueId());
			String filterType = sp.getData("filterpreference", String.class, "REGULAR");

			if (filterType.equalsIgnoreCase("none")) {
				if (fm != null)
					event.setMessage(fm.filter(event.getMessage(), FilterType.ADVERTISING, FilterType.BOT,
							FilterType.MANDATORY));
			} else if (filterType.equalsIgnoreCase("regular")) {
				if (fm != null)
					event.setMessage(fm.filter(event.getMessage(), FilterType.values()));
			}

			MSG.tell(p, player.getName() + ": " + event.getMessage());
		}

	}
}
