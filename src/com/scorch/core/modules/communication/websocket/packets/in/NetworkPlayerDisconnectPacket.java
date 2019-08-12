package com.scorch.core.modules.communication.websocket.packets.in;

import java.util.UUID;

import com.scorch.core.modules.communication.websocket.packets.BasePacket;

/**
 * A packet used when a player leaves the network
 */
public class NetworkPlayerDisconnectPacket extends BasePacket {

    private final UUID player;

    public NetworkPlayerDisconnectPacket(UUID player) {
        super();
        this.player = player;
    }

    public UUID getPlayer() {
        return player;
    }
}
