package com.scorch.core.modules.punish;

import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.commands.PunishCommand;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.DataManager;

/**
 * {@link PunishModule} Manages and loads all commands, events, etc. related to
 * the punishment system.
 * 
 * Note that the {@link DataManager} module should be loaded before this.
 * 
 * @author imodm
 *
 */
public class PunishModule extends AbstractModule {

	public PunishModule(String id) {
		super(id);
	}

	private Listener joinListener;
	private final String table = "punishments";

	@Override
	public void initialize() {
		new PunishCommand();
		joinListener = new PunishLoginListener();


		try {

			ScorchCore.getInstance().getDataManager().createTable(table, Punishment.class);
		}
		catch(NoDefaultConstructorException e){
			e.printStackTrace();
		}
	}

	@Override
	public void disable() {
		PlayerLoginEvent.getHandlerList().unregister(joinListener);
	}

	public void addPunishment(Punishment punishment) {
		punishment.execute();
	}

}
