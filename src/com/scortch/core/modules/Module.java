package com.scortch.core.modules;

public interface Module {
	void onLoad();

	void disable();

	boolean enabled();

	String getId();
}
