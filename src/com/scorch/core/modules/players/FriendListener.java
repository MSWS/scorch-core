package com.scorch.core.modules.players;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.scorch.core.ScorchCore;
import com.scorch.core.events.friends.FriendRemoveEvent;
import com.scorch.core.events.friends.FriendRequestEvent;
import com.scorch.core.utils.MSG;

public class FriendListener implements Listener {
    private FriendModule fm = ScorchCore.getInstance().getModule("FriendModule", FriendModule.class);

    public FriendListener() {
        Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
    }

    @EventHandler
    public void onFriendCreate(FriendRequestEvent event) {
        Friendship f = event.getFriendship();

        fm.addFriendship(f);

        OfflinePlayer target = Bukkit.getOfflinePlayer(f.getTarget());
        if (!target.isOnline())
            return;
        MSG.tell(target.getPlayer(), f.getPlayer() + " sent a friend request to you");
    }

    @EventHandler
    public void onFriendRemove(FriendRemoveEvent event) {
        Friendship f = event.getFriendship();

        fm.deleteFriendship(f);

        OfflinePlayer target = Bukkit.getOfflinePlayer(f.getTarget());
        if (!target.isOnline())
            return;
        MSG.tell(target.getPlayer(), f.getPlayer() + " unfriended you");
    }
}
