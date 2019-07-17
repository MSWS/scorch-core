package com.scorch.core.modules.punish;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;

public class BanwaveModule extends AbstractModule {

	public BanwaveModule(String id) {
		super(id);
	}

	private long rate, lastBanwave;
	private List<Punishment> punishments;

	private BukkitRunnable banwave;

	@Override
	public void initialize() {
		punishments = new ArrayList<Punishment>();
		rate = 1000 * 60 * 5; // 1 second * 1 minutes * 5 minutes = 5 Minute rate (milliseconds)
		// TODO may be changed later

		getBanwave().runTaskTimer(ScorchCore.getInstance(), rate, rate);
	}

	@Override
	public void disable() {
		if (punishments != null)
			punishments.clear();

		// getBanwave().cancel();
	}

	private BukkitRunnable getBanwave() {
		if (banwave != null)
			return this.banwave;
		return new BukkitRunnable() {
			@Override
			public void run() {
				punishments.forEach(Punishment::execute);
				punishments.clear();
				lastBanwave = System.currentTimeMillis();
			}
		};
	}

	public void addPunishment(Punishment p) {
		punishments.add(p);
	}

	public void runManually() {
		getBanwave().run();
	}

	public List<Punishment> getPunishments() {
		return punishments;
	}

	public long timeToNext() {
		return rate + lastBanwave - System.currentTimeMillis();
	}
}
