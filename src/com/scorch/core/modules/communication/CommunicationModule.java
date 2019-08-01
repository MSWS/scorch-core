package com.scorch.core.modules.communication;

import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.communication.exceptions.WebSocketException;
import com.scorch.core.modules.communication.websocket.SocketClient;
import com.scorch.core.modules.communication.websocket.packets.out.EventPacket;
import com.scorch.core.utils.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class CommunicationModule extends AbstractModule {

    private SocketClient websocket;

    public CommunicationModule(String id) {
        super(id);
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
            this.websocket.send(new EventPacket(event).toString());
        }
        else {
            Logger.error("Tried to dispatch a network event even though the websocket isn't connected!");
            throw new WebSocketException("Websocket state isn't open!");
        }
    }

    @Override
    public void disable() {
        this.websocket.close(1000);
    }
}
