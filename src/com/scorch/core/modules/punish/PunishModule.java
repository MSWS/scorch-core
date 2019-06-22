package com.scorch.core.modules.punish;

import com.scorch.core.modules.AbstractModule;

public class PunishModule extends AbstractModule {

	public PunishModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {
		loadPunishments();
	}

	@Override
	public void disable() {
		
	}
	
	private void loadPunishments() {
		
	}
}
