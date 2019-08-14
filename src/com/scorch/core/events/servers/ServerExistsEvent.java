package com.scorch.core.events.servers;

import com.scorch.core.modules.communication.NetworkEvent;

public class ServerExistsEvent extends NetworkEvent {

	private String server;

	public ServerExistsEvent(String serverName) {
		this.server = serverName;
	}

	public String getServer() {
		return server;
	}
}
