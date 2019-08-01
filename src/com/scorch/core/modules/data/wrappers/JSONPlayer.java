package com.scorch.core.modules.data.wrappers;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.UUID;


/**
 * Really simple JSON wrapper for {@link org.bukkit.entity.Player}
 * Used for {@link com.scorch.core.modules.communication.NetworkEvent}s
 */
public class JSONPlayer {

    private UUID uniqueId;
    private String name;
    private double health;
    private double maxHealth;

    public JSONPlayer(UUID uniqueId, String name, double health, double maxHealth) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public Player toPlayer (){
        return Bukkit.getPlayer(getUniqueId());
    }

    /**
     * Creates a new JSONPlayer from an existing player
     * @param player the player
     * @return       the jsonplayer object
     */
    public static JSONPlayer fromPlayer (Player player){
        return new JSONPlayer(player.getUniqueId(), player.getName(), player.getHealth(), player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }
}
