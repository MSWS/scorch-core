package com.scorch.core.modules.communication.websocket;

import java.net.URI;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.scorch.core.ScorchCore;
import com.scorch.core.modules.communication.NetworkEvent;
import com.scorch.core.modules.communication.exceptions.WebSocketException;
import com.scorch.core.modules.communication.websocket.packets.PacketUtils;
import com.scorch.core.modules.communication.websocket.packets.in.NetworkPlayerDisconnectPacket;
import com.scorch.core.modules.communication.websocket.packets.in.NetworkPlayerJoinPacket;
import com.scorch.core.modules.communication.websocket.packets.in.NetworkPlayerListPacket;
import com.scorch.core.modules.communication.websocket.packets.out.ConnectionPacket;
import com.scorch.core.modules.permissions.PermissionUpdateEvent;
import com.scorch.core.utils.Logger;

/**
 * A Websocket client that connects to the bungee server and handles incoming
 * {@link NetworkEvent}s
 * 
 * @author Gijs "kitsune" de Jong
 */
public class SocketClient extends WebSocketClient {

	public SocketClient(URI serverUri) {
		super(serverUri);
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		ConnectionPacket packet = new ConnectionPacket("server:" + ScorchCore.getInstance().getServer().getPort());
		Logger.log("&eConnected to websocket server, sending connection packet with name %s", packet.getServerName());
		send(packet.toString());
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					ScorchCore.getInstance().getCommunicationModule()
							.dispatchEvent(new PermissionUpdateEvent("gamergirl"));
				} catch (WebSocketException e) {
					e.printStackTrace();
				}
			}
		}.runTaskLater(ScorchCore.getInstance(), 30);
	}

	@Override
	public void onMessage(String message) {
		Gson gson = new Gson();
		Logger.log("received: " + message);

		if (!PacketUtils.isValidJSON(message)) {
			Logger.error("Received a packet that contained invalid json!\n%s", message);
			return;
		}
		if (!PacketUtils.isValidPacket(message)) {
			ScorchCore.getInstance().getLogger().info(message);
			Logger.error("Received an invalid packet!\n%s", message);
			return;
		}

		if (PacketUtils.getPacketType(message).equals("EventPacket")) {
			JsonObject event = gson.fromJson(gson.fromJson(message, JsonObject.class).get("event").getAsString(),
					JsonObject.class);
			String type = event.get("eventClassType").getAsString();
			event.remove("eventClassType");
			try {
				NetworkEvent networkEvent = (NetworkEvent) gson.fromJson(event.toString(), Class.forName(type));
				if (networkEvent != null) {
					Bukkit.getServer().getPluginManager().callEvent(networkEvent);
					Logger.log("Successfully called event");
				} else {
					Logger.error("Received invalid event gson: " + event.toString() + " (" + type + ")");
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else if (PacketUtils.getPacketType(message).equals("NetworkPlayerJoinPacket")) {
			NetworkPlayerJoinPacket packet = gson.fromJson(message, NetworkPlayerJoinPacket.class);
			ScorchCore.getInstance().getCommunicationModule().addNetworkPlayer(packet.getPlayer());
		} else if (PacketUtils.getPacketType(message).equals("NetworkPlayerDisconnectPacket")) {
			NetworkPlayerDisconnectPacket packet = gson.fromJson(message, NetworkPlayerDisconnectPacket.class);
			ScorchCore.getInstance().getCommunicationModule().removeNetworkPlayer(packet.getPlayer());
		} else if (PacketUtils.getPacketType(message).equals("NetworkPlayerListPacket")) {
			NetworkPlayerListPacket packet = gson.fromJson(message, NetworkPlayerListPacket.class);
			packet.getOnlinePlayers().forEach(player -> {
				ScorchCore.getInstance().getCommunicationModule().addNetworkPlayer(player);
			});
		}
	}

	@Override
	public void onClose(int i, String s, boolean b) {
		Logger.info("Closed websocket connection, reason: %s", s);
	}

	@Override
	public void onError(Exception e) {
		Logger.error("Error occured with websocket: " + e.getMessage());
		e.printStackTrace();
	}
}
