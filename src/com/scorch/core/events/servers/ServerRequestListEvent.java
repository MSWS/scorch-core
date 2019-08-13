package com.scorch.core.events.servers;

import com.scorch.core.modules.communication.NetworkEvent;

public class ServerRequestListEvent extends NetworkEvent {

	private String server;

	public ServerRequestListEvent(String serverName) {
		this.server = serverName;
	}

	public String getServer() {
		return server;
	}

}
