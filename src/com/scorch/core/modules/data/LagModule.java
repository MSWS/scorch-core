package com.scorch.core.modules.data;

import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;

public class LagModule extends AbstractModule {

	private int TICK_COUNT = 0;
	private long[] TICKS = new long[60000];

	public LagModule(String id) {
		super(id);
	}

	private BukkitRunnable runner;

	private long startTime;

	@Override
	public void initialize() {
		runner = run();
		runner.runTaskTimer(ScorchCore.getInstance(), 0, 1);

		startTime = System.currentTimeMillis();
	}

	@Override
	public void disable() {
		if (runner != null)
			runner.cancel();
	}

	public double getTPS(int ticks) {
		if (TICK_COUNT < ticks) {
			return 20.0D;
		}
		int target = (TICK_COUNT - 1 - ticks) % TICKS.length;
		long elapsed = System.currentTimeMillis() - TICKS[target];

		return ticks / (elapsed / 1000.0D);
	}

	public long getElapsed(int tickID) {
		if (TICK_COUNT - tickID >= TICKS.length) {
		}

		long time = TICKS[(tickID % TICKS.length)];
		return System.currentTimeMillis() - time;
	}

	public BukkitRunnable run() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				TICKS[(TICK_COUNT % TICKS.length)] = System.currentTimeMillis();

				TICK_COUNT += 1;
			}
		};
	}

	public long getUptime() {
		return System.currentTimeMillis() - startTime;
	}

}
