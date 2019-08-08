package com.scorch.core.modules.economy;

import com.scorch.core.modules.data.annotations.DataPrimaryKey;

import java.util.UUID;

/**
 * A class to make it easier to handle economy.
 * @apiNote This class should not be used outside of EconomyModule, since there's an api for handling transactions
 * and getting the player's funds, it's only public to make sure {@link com.scorch.core.modules.data.DataManager} can
 * use it!
 *
 * @see EconomyModule
 */
public class EconomyPlayer {

    @DataPrimaryKey
    private UUID uniqueId;
    private long funds;

    public EconomyPlayer(UUID uniqueId, long funds) {
        this.uniqueId = uniqueId;
        this.funds = funds;
    }

    public EconomyPlayer() {

    }

    /**
     * Gets the unique id of the player
     * @return the unique id of the player
     */
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Gets the funds of the player
     * @return
     */
    public long getFunds() {
        return funds;
    }

    /**
     * Sets the funds of the player;
     * @param funds the new amount
     */
    public void setFunds(long funds) {
        this.funds = funds;
    }
}
