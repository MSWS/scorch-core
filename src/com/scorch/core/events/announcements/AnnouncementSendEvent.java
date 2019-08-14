package com.scorch.core.events.announcements;

import com.scorch.core.modules.communication.NetworkEvent;

public class AnnouncementSendEvent extends NetworkEvent {
	private String server, perm, message;

	public AnnouncementSendEvent(String server, String perm, String message) {
		this.server = server;
		this.perm = perm;
		this.message = message;
	}

	public String getServer() {
		return server;
	}

	public String getPermission() {
		return perm;
	}

	public String getMessage() {
		return message;
	}

	public void setServer(String server) {
		this.server = server;
	}
}
