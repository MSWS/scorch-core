package com.scorch.core.events.punishment;

import com.scorch.core.modules.communication.NetworkEvent;
import com.scorch.core.modules.punish.Punishment;

public class TestEvent extends NetworkEvent {
	private Punishment p;

	public TestEvent(Punishment p) {
		this.p = p;
	}

	public Punishment getPunishment() {
		return p;
	}
}
