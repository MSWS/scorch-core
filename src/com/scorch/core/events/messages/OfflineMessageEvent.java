package com.scorch.core.events.messages;

import com.scorch.core.modules.communication.NetworkEvent;
import com.scorch.core.modules.messages.OfflineMessage;

public class OfflineMessageEvent extends NetworkEvent {
	private OfflineMessage message;

	public OfflineMessageEvent(OfflineMessage msg) {
		this.message = msg;
	}

	public OfflineMessage getMessage() {
		return message;
	}

}
