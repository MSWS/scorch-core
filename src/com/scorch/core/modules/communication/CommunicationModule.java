package com.scorch.core.modules.communication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.communication.exceptions.WebSocketException;
import com.scorch.core.modules.communication.websocket.SocketClient;
import com.scorch.core.modules.communication.websocket.packets.out.EventPacket;
import com.scorch.core.utils.Logger;
import org.bukkit.entity.Player;

public class CommunicationModule extends AbstractModule {

    private SocketClient websocket;

    private List<UUID> networkPlayers;

    private Gson eventGson;


    public CommunicationModule(String id) {
        super(id);
        this.networkPlayers = new ArrayList<>();
        this.eventGson = new GsonBuilder().registerTypeAdapter(NetworkEvent.class, new NetworkEventSerializer()).addSerializationExclusionStrategy(new ExcludeStrategy()).create();
    }

    @Override
    public void initialize() {
        Logger.log("Setting up websocket connection to bungee server...");
        try {
            this.websocket = new SocketClient(new URI("ws://localhost:6969"));
            this.websocket.connect();
        } catch (URISyntaxException e) {
            Logger.error("Invalid uri for websocket!");
        }
    }

    /**
     * Dispatches the event across the bungee network by converting the the event given to json
     * using {@link com.google.gson.Gson}. Use the {@link com.scorch.core.modules.data.annotations.DataIgnore} annotation
     * to ignore any data you want to be ignored
     * @param event the event
     * @throws WebSocketException
     */
    public void dispatchEvent (NetworkEvent event) throws WebSocketException {
        if(this.websocket.isOpen()){
            EventPacket packet = new EventPacket(event);
			this.websocket.send(eventGson.toJson(packet));
        }
        else {
            Logger.error("Tried to dispatch a network event even though the websocket isn't connected!");
            throw new WebSocketException("Websocket state isn't OPEN!");
        }
    }

    /**
     * Returns whether the target player is online on the bungee network
     * @param player the target player
     * @return       whether the target player is online or not
     */
    public boolean isOnline (UUID player){
        return networkPlayers.contains(player);
    }

    /**
     * Returns whether the target player is online on the bungee network
     * @param player the target player
     * @return       whether the target player is online or not
     */
    public boolean isOnline (Player player){
        return isOnline(player.getUniqueId());
    }

    /**
     * Adds a player to the network player list, this will make {@link CommunicationModule#isOnline(UUID)} return true
     * for the given uuid
     * @param uuid the player to add to the list
     */
    public void addNetworkPlayer (UUID uuid){
        if(this.networkPlayers.contains(uuid)) return;
        this.networkPlayers.add(uuid);
    }

    /**
     * Removes a player from the network player list, this will make {@link CommunicationModule#isOnline(UUID)} return
     * false for the given uuid
     * @param uuid the player to remove from the list
     */
    public void removeNetworkPlayer (UUID uuid){
        if(!this.networkPlayers.contains(uuid)) return;
        this.networkPlayers.remove(uuid);
    }

    @Override
    public void disable() {
        this.websocket.close(1000, "server shutdown");
    }
}
