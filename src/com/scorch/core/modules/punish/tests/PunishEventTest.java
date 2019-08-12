package com.scorch.core.modules.punish.tests;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.scorch.core.ScorchCore;
import com.scorch.core.events.punishment.PunishmentCreateEvent;
import com.scorch.core.events.punishment.PunishmentUpdateEvent;
import com.scorch.core.modules.communication.exceptions.WebSocketException;
import com.scorch.core.modules.punish.PunishType;
import com.scorch.core.modules.punish.Punishment;
import com.scorch.core.utils.Logger;

public class PunishEventTest implements Listener {

    public PunishEventTest () {
        ScorchCore.getInstance().getServer().getPluginManager().registerEvents(this, ScorchCore.getInstance());
        Punishment testPunishment = new Punishment(UUID.randomUUID(), "TEST CASE", "TEST EVENT", System.currentTimeMillis(), 300, PunishType.OTHER);

        try {
            Logger.info("DISPATCHING PunishmentCreateEvent to network!");
            ScorchCore.getInstance().getCommunicationModule().dispatchEvent(new PunishmentCreateEvent(testPunishment));
            Logger.info("DISPATCHING PunishmentUpdateEvent to network!");
            ScorchCore.getInstance().getCommunicationModule().dispatchEvent(new PunishmentUpdateEvent(testPunishment));
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPunishEvent (PunishmentCreateEvent e){
        Logger.info("PunishCreateEvent triggered: " + e.getPunishment().getReason());
    }

    public void onPunishUpdateEvent (PunishmentUpdateEvent e){
        Logger.info("PunishmentUpdateEvent triggered: " + e.getPunishment().getReason());
    }

}
