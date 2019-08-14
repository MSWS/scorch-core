package com.scorch.core.modules.communication.websocket.packets.in;

import java.util.List;
import java.util.UUID;

import com.scorch.core.modules.communication.websocket.packets.BasePacket;

/**
 * A packet used when a server connects to the network and needs a list of all the players online on the network
 */
public class NetworkPlayerListPacket extends BasePacket {

    private final List<UUID> onlinePlayers;

    public NetworkPlayerListPacket(List<UUID> onlinePlayers) {
        super();
        this.onlinePlayers = onlinePlayers;
    }

    public List<UUID> getOnlinePlayers() {
        return onlinePlayers;
    }
}
