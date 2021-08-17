package com.scorch.core.events.messages;

import com.scorch.core.modules.messages.OfflineMessage;

public class OfflineMessageCreateEvent extends OfflineMessageEvent {

	public OfflineMessageCreateEvent(OfflineMessage msg) {
		super(msg);
	}

}
