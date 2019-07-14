package com.scorch.core.modules.messages;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.scorch.core.modules.data.annotations.DataNotNull;
import com.scorch.core.utils.MSG;

public class CMessage {
	@DataNotNull
	private String msg;
	@DataNotNull
	private String id;

	public CMessage() {
	}

	public CMessage(String id, String raw) {
		this.id = id;
		msg = MSG.color(raw);
	}

	public CMessage(String id, String raw, Map<String, String> holders) {
		this(id, raw);
		applyPlaceholders(holders);
	}

	public String format(Player player) {
		// TODO
		return msg;
	}

	public String format(CommandSender sender) {
		// TODO
		return msg;
	}

	public String getId() {
		return id;
	}

	public void applyPlaceholders(Map<String, String> holders) {
		for (Entry<String, String> entries : holders.entrySet()) {
			msg = msg.replace(entries.getKey(), entries.getValue());
		}
	}

}
