package com.scorch.core.modules.economy;

import com.scorch.core.ScorchCore;
import com.scorch.core.utils.StringUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyTransaction {

    private String id;
    private long amount;
    private boolean executed;
    private boolean success;
    private UUID player;
    private String reason;
    private long fundsBefore;
    private long fundsAfter;

    /**
     * Empty constructor for the datamanager to work
     */
    public EconomyTransaction () {

    }

    /**
     * Creates a new economy transaction
     * @param player the player
     * @param amount the amount of the transaction
     * @param reason the reason for the transaction
     */
    public EconomyTransaction (UUID player, long amount, String reason) {
        // 8^62 = A LOT OF POSSIBLE IDS
        this.id = StringUtils.getUniqueString(8);
        this.player = player;
        this.reason = reason;
        this.amount = amount;
        this.fundsBefore = ScorchCore.getInstance().getEconomy().getFunds(player);
        this.fundsAfter = fundsBefore - amount;
        this.executed = false;
    }

    /**
     * Creates a new economy transaction
     * @param player the player
     * @param amount the amount of the transaction
     * @param reason the reason for the transaction
     */
    public EconomyTransaction(Player player, long amount, String reason) {
        this(player.getUniqueId(), amount, reason);
    }

    /**
     * Creates a new economy transaction without a reason given
     * @param player the player
     * @param amount the amount of the transaction
     */
    public EconomyTransaction (UUID player, long amount){
        this(player, amount, "No reason given.");
    }

    /**
     * Creates a new economy transaction without a reason given
     * @param player the player
     * @param amount the amount of the transaction
     */
    public EconomyTransaction (Player player, long amount){
        this(player.getUniqueId(), amount);
    }


    public boolean execute () {
        if(isExecuted()) return isSuccess();
        long funds = ScorchCore.getInstance().getEconomy().getFunds(getPlayer());
        if(funds >= getAmount()){
            // transaction can happen
            this.success = true;

            // update player's funds and store transaction in database
            ScorchCore.getInstance().getEconomy().setFunds(getPlayer(), getFundsAfter());
            ScorchCore.getInstance().getDataManager().saveObjectAsync("transactions", this);
        }
        else {
            // not enough funds save tr
            this.success = false;
            ScorchCore.getInstance().getDataManager().saveObjectAsync("transactions", this);
            return false;
        }
        return false;
    }


    public long getAmount() {
        return amount;
    }

    public boolean isExecuted() {
        return executed;
    }

    public boolean isSuccess() {
        return success;
    }

    public UUID getPlayer() {
        return player;
    }

    public String getReason() {
        return reason;
    }

    public long getFundsBefore() {
        return fundsBefore;
    }

    public long getFundsAfter() {
        return fundsAfter;
    }
}
