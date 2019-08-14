package com.scorch.core.modules.communication.websocket.packets;

import com.google.gson.GsonBuilder;
import com.scorch.core.ScorchCore;
import com.scorch.core.modules.communication.ExcludeStrategy;

/**
 * Represents a base packet that can be sent/received by the Websocket connection
 * @author Gijs "kitsune" de Jong
 */
public abstract class BasePacket {

    private String type;
    private long timestamp;
    private String version;

    public BasePacket () {
        this.type = this.getClass().getSimpleName();
        this.timestamp = System.currentTimeMillis();
        this.version = ScorchCore.getInstance().getDescription().getVersion();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString () {
        return new GsonBuilder().addSerializationExclusionStrategy(new ExcludeStrategy()).create().toJson(this);
    }

}
