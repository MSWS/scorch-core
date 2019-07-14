package com.scorch.core.modules.permissions;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * A permissions handler
 *
 */
public class PermissionModule extends AbstractModule implements Listener {

    private Map<UUID, PermissionAttachment> permissionAttachments;
    private Map<UUID, PermissionPlayer> playerPermissions;
    private Collection<PermissionGroup> groupList;

    public PermissionModule(String id) {
        super(id);
    }

    @Override
    public void initialize() {

        // Order doesn't really matter so using HashMap
        this.permissionAttachments = new HashMap<>();
        this.playerPermissions = new HashMap<>();

        // Setup database stuff
        try {
            ScorchCore.getInstance().getDataManager().createTable("groups", PermissionGroup.class);
            ScorchCore.getInstance().getDataManager().createTable("permissions", PermissionPlayer.class);

            ScorchCore.getInstance().getDataManager().getAllObjects("permissions").forEach(obj -> {
                PermissionPlayer player = (PermissionPlayer) obj;
                playerPermissions.put(player.getUniqueId(), player);
            });

            this.groupList = ScorchCore.getInstance().getDataManager().getAllObjects("groups");
        } catch (NoDefaultConstructorException | DataObtainException e) {
            e.printStackTrace();
        }

        if(this.groupList == null){
            this.groupList = new ArrayList<>();
        }

        // Make sure this plugin is a registered listener
        Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerLogin (PlayerLoginEvent e){
        // Load the player's group and other permissions and give them to the player
        if(playerPermissions.containsKey(e.getPlayer().getUniqueId())){
            // player has joined server before
            // Give them their permissions
            this.permissionAttachments.put(e.getPlayer().getUniqueId(),
                    playerPermissions.get(e.getPlayer().getUniqueId()).toPermissionAttachment(e.getPlayer()));
        }
        else {
            // give player default group create required objects
            // TODO Implement default group and implement this
        }
    }

    @Override
    public void disable() {

    }

    public PermissionGroup getGroup (String groupName){
        for(PermissionGroup group : groupList){
            if(group.getGroupName().equalsIgnoreCase(groupName)){
                return group;
            }
        }
        return null;
    }
}
