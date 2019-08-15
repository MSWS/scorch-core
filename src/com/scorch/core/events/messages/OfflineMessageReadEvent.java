package com.scorch.core.events.messages;

import com.scorch.core.modules.messages.OfflineMessage;

public class OfflineMessageReadEvent extends OfflineMessageEvent {

	public OfflineMessageReadEvent(OfflineMessage msg) {
		super(msg);
	}

}
