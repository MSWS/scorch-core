package com.scorch.core.modules.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.DataUpdateException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;

public class IPTracker extends AbstractModule {

	private Map<UUID, IPEntry> links;

	public IPTracker(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		links = new HashMap<UUID, IPEntry>();

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					ScorchCore.getInstance().getDataManager().createTable("playerips", IPEntry.class);
					for (Object entry : ScorchCore.getInstance().getDataManager().getAllObjects("playerips")) {
						IPEntry ipe = (IPEntry) entry;
						links.put(ipe.getUUID(), ipe);
					}

				} catch (NoDefaultConstructorException | DataObtainException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());

		new BukkitRunnable() {
			@Override
			public void run() {
				saveIps();
			}
		}.runTaskTimerAsynchronously(ScorchCore.getInstance(), 6000, 6000);
	}

	@Override
	public void disable() {

	}

	public void addIp(UUID account, String ip) {
		IPEntry entry = links.getOrDefault(account, new IPEntry(account, new ArrayList<>()));

		entry.addIp(ip);

		if (!links.containsKey(account))
			ScorchCore.getInstance().getDataManager().saveObjectAsync("playerips", entry);

		links.put(account, entry);
	}

	public void addIp(UUID account, List<String> ips) {
		IPEntry entry = links.getOrDefault(account, new IPEntry(account, new ArrayList<>()));

		entry.addIps(ips);

		if (!links.containsKey(account))
			ScorchCore.getInstance().getDataManager().saveObjectAsync("playerips", entry);

		links.put(account, entry);
	}

	public Set<UUID> linkedAccounts(UUID account) {
		return linkedAccounts(account, null);
	}

	public Set<UUID> linkedAccounts(UUID account, Set<String> scannedIPs) {
		Set<UUID> accounts = new HashSet<>();
		Set<String> currentIPs = new HashSet<>(links.get(account).getIps());

		if (scannedIPs == null)
			scannedIPs = new HashSet<>();

		if (currentIPs == null || currentIPs.isEmpty()) {
			return accounts;
		}

		for (String ip : currentIPs) {
			if (scannedIPs.contains(ip)) {
				continue;
			} else {
				scannedIPs.add(ip);
				// Scan all of mapIp looking for any accounts that have used the same IP
				for (Entry<UUID, IPEntry> entry : links.entrySet()) {
					if (entry.getValue().getIps().contains(ip)) {
						accounts.add(entry.getKey());
						accounts.addAll(linkedAccounts(entry.getKey(), scannedIPs));
					}
				}
			}
		}
		return accounts;
	}

	public Set<UUID> linkedAccounts(String ip) {

		Set<UUID> accounts = getAccountsWithIP(ip);
		Set<UUID> result = new HashSet<>();
		accounts.forEach((acc) -> result.addAll(linkedAccounts(acc)));

		return result;
	}

	public Set<UUID> getAccountsWithIP(String ip) {
		return links.values().stream().filter(ipe -> ipe.getIps().contains(ip)).map(ipe -> ipe.getUUID())
				.collect(Collectors.toSet());
	}

	public Set<String> getIps(UUID account, Set<String> scannedIPs) {
		Set<UUID> accounts = new HashSet<>();
		Set<String> currentIPs = new HashSet<>(links.get(account).getIps());

		if (scannedIPs == null)
			scannedIPs = new HashSet<>();

		if (currentIPs == null || currentIPs.isEmpty()) {
			return null;
		}

		for (String ip : currentIPs) {
			if (scannedIPs.contains(ip)) {
				continue;
			} else {
				scannedIPs.add(ip);
				// Scan all of mapIp looking for any accounts that have used the same IP
				for (Entry<UUID, IPEntry> entry : links.entrySet()) {
					if (entry.getValue().getIps().contains(ip)) {
						accounts.add(entry.getKey());
						accounts.addAll(linkedAccounts(entry.getKey(), scannedIPs));
					}
				}
			}
		}
		return scannedIPs;
	}

	public boolean isLinked(UUID mainAccount, UUID altAccount) {
		return linkedAccounts(mainAccount).contains(altAccount);
	}

	public void addDummies(int accounts) {
		ThreadLocalRandom rnd = ThreadLocalRandom.current();

		for (int i = 0; i < accounts; i++) {

			UUID uuid = UUID.randomUUID();

			int ipAmo = rnd.nextInt(10);

			List<String> ips = new ArrayList<>();

			for (int a = 0; a < ipAmo; a++) {
				String ip = rnd.nextInt(10) + "." + rnd.nextInt(10) + "." + rnd.nextInt(10) + "." + rnd.nextInt(10);
				ips.add(ip);
			}
			addIp(uuid, ips);
		}
	}

	public void saveIps() {
		try {
			for (IPEntry ent : links.values()) {
				ScorchCore.getInstance().getDataManager().updateObject("playerips", ent,
						new SQLSelector("uuid", ent.getUUID()));
			}
		} catch (DataUpdateException e) {
			e.printStackTrace();
		}
	}

}
