package com.scorch.core.modules.communication;


import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.google.gson.GsonBuilder;
import com.scorch.core.modules.data.annotations.DataIgnore;

/**
 * TODO Figure out a way to hook into Spigot's event api, using messaging channels.
 *
 * @apiNote REALLY REALLY REALLY WIP
 */
public class NetworkEvent extends Event {

    @DataIgnore
    private static HandlerList handlerList = new HandlerList();

    // actual communication event stuff here


    @Override
    public String toString () {
        return new GsonBuilder().addSerializationExclusionStrategy(new ExcludeStrategy()).registerTypeAdapter(NetworkEvent.class, new NetworkEventSerializer()).create().toJson(this);
    }


    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
