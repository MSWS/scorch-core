package com.scorch.core.modules.communication;


import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * TODO Figure out a way to hook into Spigot's event api, using messaging channels.
 *
 * @apiNote REALLY REALLY REALLY WIP
 */
public class CommunicationEvent extends Event {

    private HandlerList handlerList;

    // actual communication event stuff here


    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
