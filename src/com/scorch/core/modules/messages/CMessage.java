package com.scorch.core.modules.messages;

import java.util.Map;
import java.util.Map.Entry;

import com.scorch.core.modules.data.annotations.DataPrimaryKey;
import org.bukkit.command.CommandSender;

import com.scorch.core.modules.data.annotations.DataNotNull;
import com.scorch.core.utils.MSG;

/**
 * CMessage is an Object to make storing messages in a database easier
 * 
 * @author imodm
 *
 */
public class CMessage {
	@DataNotNull
	private String msg;
	@DataNotNull @DataPrimaryKey
	private String id;

	public CMessage() {
	}

	public CMessage(String id, String raw) {
		this.id = id;
		msg = raw;
	}

	public CMessage(String id, String raw, Map<String, String> holders) {
		this(id, raw);
		applyPlaceholders(holders);
	}

	public void send(CommandSender sender) {
		sender.sendMessage(msg);
	}

	public String getMessage() {
		return MSG.color(msg);
	}

	public String getId() {
		return id;
	}

	public void applyPlaceholders(Map<String, String> holders) {
		for (Entry<String, String> entries : holders.entrySet()) {
			msg = msg.replace(entries.getKey(), entries.getValue());
		}
	}

	@Override
	public String toString() {
		return msg;
	}
}
