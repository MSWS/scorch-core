package com.scorch.core.modules.messages;

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
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;

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
				Logger.log("&9Loading offline messages...");
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

				Logger.log("&aSuccessfully loaded &e" + linked.size() + " &aoffline message"
						+ (linked.size() == 1 ? "" : "s") + ".");
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
	}

	public List<OfflineMessage> getMessages(UUID player) {
		return linked.getOrDefault(player, new ArrayList<>());
	}

	public List<OfflineMessage> getActiveMessages(UUID player) {
		return linked.getOrDefault(player, new ArrayList<>()).stream().filter(r -> !r.received())
				.collect(Collectors.toList());
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

		ScorchCore.getInstance().getDataManager().updateObjectAsync("offlinemessages", newM,
				new SQLSelector("sender", newM.getSender()), new SQLSelector("receiver", newM.getReceiver()),
				new SQLSelector("message", newM.getMessage()), new SQLSelector("sent", newM.getSentTime()));
	}
}
