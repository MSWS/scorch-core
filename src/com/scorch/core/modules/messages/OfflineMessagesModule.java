package com.scorch.core.modules.messages;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;

public class OfflineMessagesModule extends AbstractModule {

	public OfflineMessagesModule(String id) {
		super(id);
	}

	private List<OfflineMessage> offline;

	private Listener offlineJoinListener;

	private Map<UUID, List<OfflineMessage>> linked;

	@Override
	public void initialize() {
		offline = new ArrayList<OfflineMessage>();
		linked = new HashMap<>();

		offlineJoinListener = new OfflineJoinListener();

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					ScorchCore.getInstance().getDataManager().createTable("offlinemessages", OfflineMessage.class);
					ScorchCore.getInstance().getDataManager().getAllObjects("offlinemessages").forEach(msg -> {
						OfflineMessage om = (OfflineMessage) msg;
						offline.add(om);
						List<OfflineMessage> temp = linked.getOrDefault(om.getReceiver(), new ArrayList<>());
						temp.add(om);
						linked.put(om.getReceiver(), temp);
					});
				} catch (DataObtainException | NoDefaultConstructorException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
	}

	public List<OfflineMessage> getMessages(UUID player) {
		return linked.get(player);
	}

	public List<OfflineMessage> getActiveMessages(UUID player) {
		return linked.get(player).stream().filter(r -> !r.received()).collect(Collectors.toList());
	}

	@Override
	public void disable() {
		offline.clear();
		PlayerJoinEvent.getHandlerList().unregister(offlineJoinListener);
	}

	public void addMessage(OfflineMessage msg) {
		offline.add(msg);

		List<OfflineMessage> temp = linked.getOrDefault(msg.getReceiver(), new ArrayList<>());
		temp.add(msg);
		linked.put(msg.getReceiver(), temp);

		new BukkitRunnable() {
			@Override
			public void run() {
				ScorchCore.getInstance().getDataManager().saveObject("offlinemessages", msg);
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
	}

	public void update(OfflineMessage old, OfflineMessage newM) {
		offline.remove(old);
		offline.add(newM);

		PreparedStatement prepared = ScorchCore.getInstance().getDataManager().getConnectionManager("easytoremember")
				.prepareStatement(
						"UPDATE offlinemessages SET received = ? WHERE sender = ? AND receiver = ? AND message = ? AND sent = ?");

		try {
			prepared.setLong(1, newM.getReceivedTime());
			prepared.setString(2, newM.getSender());
			prepared.setString(3, newM.getReceiver() + "");
			prepared.setString(4, newM.getMessage());
			prepared.setLong(5, newM.getSentTime());

			prepared.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
