package com.scorch.core.modules.messages;

import java.util.UUID;

import com.scorch.core.modules.data.annotations.DataPrimaryKey;

public class OfflineMessage implements Comparable<OfflineMessage> {
	@DataPrimaryKey
	private UUID uuid;

	private String sender;
	private UUID receiver;
	private String message;

	private long sent, received;

	public OfflineMessage() {
	}

	public OfflineMessage(String sender, UUID receiver, String message) {
		this.uuid = UUID.randomUUID();
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
		this.sent = System.currentTimeMillis();
	}

	public OfflineMessage(String sender, UUID receiver, String message, long sent, long received) {
		this(sender, receiver, message);
		this.sent = sent;
		this.received = received;
	}

	public boolean received() {
		return received != 0;
	}

	public long getSentTime() {
		return sent;
	}

	public long getReceivedTime() {
		return received;
	}

	public String getSender() {
		return sender;
	}

	public UUID getReceiver() {
		return receiver;
	}

	public String getMessage() {
		return message;
	}

	public OfflineMessage read() {
		received = System.currentTimeMillis();
		return this;
	}

	@Override
	public int compareTo(OfflineMessage o) {
		return o.getSentTime() < sent ? 1 : -1;
	}

}
