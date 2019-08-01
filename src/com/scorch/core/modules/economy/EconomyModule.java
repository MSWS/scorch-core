package com.scorch.core.modules.economy;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

/**
 * An economy implementation for ScorchGamez this class will handle all the in-game currency related transactions
 * TODO: Sync currency updates across servers using events
 */
public class EconomyModule extends AbstractModule {

    private HashMap<UUID, EconomyPlayer> playerFunds;


    public EconomyModule(String id) {
        super(id);

        this.playerFunds = new HashMap<>();
    }

    @Override
    public void initialize() {
        try {
            // Create database table
            ScorchCore.getInstance().getDataManager().createTable("economy", EconomyPlayer.class);
            ScorchCore.getInstance().getDataManager().createTable("transactions", EconomyTransaction.class);

            Collection<EconomyPlayer> playerEconomies = ScorchCore.getInstance().getDataManager().getAllObjects("economy");
            playerEconomies.forEach(playerFund -> {
                if(!playerFunds.containsKey(playerFund.getUniqueId())){
                    playerFunds.put(playerFund.getUniqueId(), playerFund);
                }
                else {
                    Logger.warn("Duplicate player funds for uuid = %s", playerFund.getUniqueId());
                }
            });

            Logger.info("&aLoaded &e%s &aplayer funds!", playerFunds.size());

        } catch (NoDefaultConstructorException | DataObtainException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disable() {
        // Don't need to save funds, because funds get saved asynchronously when they got modified
    }

    /**
     * Creates a new {@link EconomyTransaction} for the player
     * @param player the player
     * @param amount the amount of the transaction
     * @param reason the reason for the transaction
     * @return       the economy transaction
     */
    public EconomyTransaction createTransaction (UUID player, long amount, String reason) {
        return new EconomyTransaction(player, amount, reason);
    }

    /**
     * Creates a new {@link EconomyTransaction} for the player
     * @param player the player
     * @param amount the amount of the transaction
     * @param reason the reason for the transaction
     * @return       the economy transaction
     */
    public EconomyTransaction createTransaction (Player player, long amount, String reason){
        return createTransaction(player.getUniqueId(), amount, reason);
    }

    /**
     * Creates a new {@link EconomyTransaction} for the player without a reason given
     * @param player the player
     * @param amount the amount of the transaction
     * @return       the economy transaction
     */
    public EconomyTransaction createTransaction (UUID player, long amount){
        return new EconomyTransaction(player, amount);
    }

    /**
     * Creates a new {@link EconomyTransaction} for the player without a reason given
     * @param player the player
     * @param amount the amount of the transaction
     * @return       the economy transaction
     */
    public EconomyTransaction createTransaction (Player player, long amount){
        return this.createTransaction(player.getUniqueId(), amount);
    }

    /**
     * Gets the player's funds
     * @param player the player
     * @return       the player's funds
     */
    public long getFunds (UUID player){
        if(!playerFunds.containsKey(player)){
            playerFunds.put(player, new EconomyPlayer(player, 0));
            updateFunds(player);
        }
        return playerFunds.get(player).getFunds();
    }


    /**
     * Gets the player's funds
     * @param player the player
     * @return       the player's funds
     */
    public long getFunds (Player player){
        return getFunds(player.getUniqueId());
    }

    /**
     * Sets the funds of the player to the specified amount
     * @param player the player
     * @param amount the new amount of funds
     */
    public void setFunds(UUID player, long amount){
        if(playerFunds.containsKey(player)){
            playerFunds.get(player).setFunds(amount);
        }
        else {
            playerFunds.put(player, new EconomyPlayer(player, amount));
        }
        updateFunds(player);
    }

    /**
     * Sets the funds of the player to the specified amount
     * @param player the player
     * @param amount the new amount of funds
     */
    public void setFunds (Player player, long amount){
        setFunds(player.getUniqueId(), amount);
    }

    /**
     * Adds the funds to the player's balance
     * @param player the player
     * @param amount the amount of funds to add
     */
    public void addFunds (UUID player, long amount){
        setFunds(player, getFunds( player ) + amount);
    }

    /**
     * Adds the funds to the player's balance
     * @param player the player
     * @param amount the amount of funds to add
     */
    public void addFunds (Player player, long amount){
        addFunds(player, amount);
    }

    /**
     * Removes the funds from the player's balance, this doesn't check if the amount can be removed without being
     * in the negative!
     * @param player the player
     * @param amount the amount of funds to remove
     */
    public void removeFunds (UUID player, long amount){
        setFunds(player, getFunds(player) - amount);
    }

    /**
     * Removes the funds from the player's balance, this doesn't check if the amount can be removed without being
     * in the negative!
     * @param player the player
     * @param amount the amount of funds to remove
     */
    public void removeFunds (Player player, long amount){
        removeFunds(player.getUniqueId(), amount);
    }

    /**
     * Updates the specified player's funds in the database
     * @param player the player
     */
    private void updateFunds (UUID player){
        if(!playerFunds.containsKey(player)){
            playerFunds.put(player, new EconomyPlayer(player, 0));
        }
        ScorchCore.getInstance().getDataManager().updateObjectAsync("economy", playerFunds.get(player), new SQLSelector("uniqueId", player));
    }
}
