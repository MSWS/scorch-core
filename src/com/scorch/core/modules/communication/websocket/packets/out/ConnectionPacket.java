package com.scorch.core.modules.communication.websocket.packets.out;

import com.scorch.core.modules.communication.websocket.packets.BasePacket;

/**
 * A basic authorisation packet that's used to identify servers on the websocket server
 */
public class ConnectionPacket extends BasePacket {

    private String serverName;

    public ConnectionPacket(String serverName) {
        super();
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}

