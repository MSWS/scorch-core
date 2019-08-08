package com.scorch.core.modules.players;

import com.scorch.core.modules.data.annotations.DataPrimaryKey;

import java.util.List;
import java.util.UUID;

public class IPEntry {

	@DataPrimaryKey
	private UUID uuid;
	private List<String> ips;

	public IPEntry() {

	}

	public IPEntry(UUID uuid, List<String> ips) {
		this.uuid = uuid;
		this.ips = ips;
	}

	public void addIp(String ip) {
		ips.add(ip);
	}

	public void addIps(List<String> ips) {
		this.ips.addAll(ips);
	}

	public List<String> getIps() {
		return ips;
	}

	public UUID getUUID() {
		return uuid;
	}
}
