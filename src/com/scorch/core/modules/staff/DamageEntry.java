package com.scorch.core.modules.staff;

import com.scorch.core.utils.MSG;

public class DamageEntry {
	private String damager, weapon;

	private long time;

	private double damageAmount;

	public DamageEntry(String damager, double dmgAmo) {
		this.damager = damager;
		this.weapon = "Unknown";
		this.time = System.currentTimeMillis();
		this.damageAmount = dmgAmo;
	}

	public DamageEntry(String damager, double dmgAmo, String weapon) {
		this(damager, dmgAmo);
		this.weapon = weapon;
	}

	public String getDamager() {
		return damager;
	}

	public String getWeapon() {
		return weapon;
	}

	public long getTime() {
		return time;
	}

	public long getTimeElapsed() {
		return System.currentTimeMillis() - time;
	}

	public double getDamageAmount() {
		return damageAmount;
	}

	public String format() {
		if (weapon.equals("Unknown")) {
			return "&c" + damager + " &7dealt &e" + getDamageAmount() + " &7damage &7[&e"
					+ (getTimeElapsed() > 500 ? MSG.getTime(getTimeElapsed()) + " ago" : "Just Now") + "&7]";
		} else {
			return "&c" + damager + " &7dealt &e" + getDamageAmount() + " &7damage with &a" + weapon + " &7[&e"
					+ (getTimeElapsed() > 500 ? MSG.getTime(getTimeElapsed()) + " ago" : "Just Now") + "&7]";
		}

	}
}
