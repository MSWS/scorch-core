package com.scorch.core.modules.communication.websocket.packets.in;

import com.scorch.core.modules.communication.websocket.packets.BasePacket;

import java.util.UUID;

/**
 * A packet used when a player joins the network
 */
public class NetworkPlayerJoinPacket extends BasePacket {

    private final UUID player;

    public NetworkPlayerJoinPacket(UUID player) {
        super();
        this.player = player;
    }

    public UUID getPlayer() {
        return player;
    }
}
