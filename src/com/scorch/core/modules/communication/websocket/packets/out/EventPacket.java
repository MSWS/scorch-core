package com.scorch.core.modules.communication.websocket.packets.out;

import com.google.gson.GsonBuilder;
import com.scorch.core.modules.communication.ExcludeStrategy;
import com.scorch.core.modules.communication.NetworkEvent;
import com.scorch.core.modules.communication.NetworkEventSerializer;
import com.scorch.core.modules.communication.websocket.packets.BasePacket;

public class EventPacket extends BasePacket {

    private NetworkEvent event;

    public EventPacket(NetworkEvent event) {
        this.event = event;
    }

    public NetworkEvent getEvent() {
        return event;
    }

    public void setEvent(NetworkEvent event) {
        this.event = event;
    }
}
