package com.scorch.core.modules.punish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.scorch.core.ScorchCore;
import com.scorch.core.commands.HistoryCommand;
import com.scorch.core.commands.PunishCommand;
import com.scorch.core.commands.UnpunishCommand;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.DataManager;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;

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

	private List<Punishment> punishments;

	private Map<UUID, List<Punishment>> linked = new HashMap<UUID, List<Punishment>>();

	@Override
	public void initialize() {
		new PunishCommand();
		new HistoryCommand();
		new UnpunishCommand();

		joinListener = new PunishLoginListener();

		punishments = new ArrayList<Punishment>();

		try {
			ScorchCore.getInstance().getDataManager().createTable(table, Punishment.class);

			ScorchCore.getInstance().getDataManager().getAllObjects("punishments").forEach(punish -> {
				Punishment p = (Punishment) punish;
				punishments.add(p);

				List<Punishment> current = linked.getOrDefault(p.getTargetUUID(), new ArrayList<>());
				current.add(p);

				linked.put(p.getTargetUUID(), current);
			});
		} catch (NoDefaultConstructorException | DataObtainException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void disable() {
		PlayerLoginEvent.getHandlerList().unregister(joinListener);
	}

	public void addPunishment(Punishment punishment) {
		punishment.execute();

		List<Punishment> current = linked.getOrDefault(punishment.getTargetUUID(), new ArrayList<>());
		current.add(punishment);

		linked.put(punishment.getTargetUUID(), current);

		ScorchCore.getInstance().getDataManager().saveObject("punishments", punishment);
	}

	public List<Punishment> getPunishments(UUID player) {
		return linked.get(player);
	}

}
