package com.scorch.core.modules.permissions;

import com.scorch.core.ScorchCore;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * PermissionPlayer is a data class that contains all the data needed for the {@link PermissionModule} to load extra
 * permissions a player might have besides their group permissions.
 *
 * @see PermissionModule
 * @see PermissionGroup
 */
public class PermissionPlayer {

    private UUID uniqueId;
    private String group;
    private List<String> permissions;

    /**
     * Default constructor for {@link com.scorch.core.modules.data.DataManager} to work
     */
    public PermissionPlayer () { }

    /**
     * PermissionPlayer is a data class that contains all the data needed for the {@link PermissionModule} to load extra
     * permissions a player might have besides their group permissions
     * @param uniqueId    the uuid of the player
     * @param group       the group of the player
     * @param permissions the permissions to add
     *
     * @see PermissionModule
     * @see PermissionGroup
     */
    public PermissionPlayer (UUID uniqueId, String group, String... permissions){
        this.uniqueId = uniqueId;
        this.group = group;
        this.permissions = new ArrayList<>(Arrays.asList(permissions));
    }

    /**
     * PermissionPlayer is a data class that contains all the data needed for the {@link PermissionModule} to load extra
     * permissions a player might have besides their group permissions
     * @param player      the player
     * @param group       the group of the player
     * @param permissions the permissions to add
     *
     * @see PermissionModule
     * @see PermissionGroup
     */
    public PermissionPlayer(Player player, String group, String... permissions){
        this(player.getUniqueId(), group, permissions);
    }

    /**
     * PermissionPlayer is a data class that contains all the data needed for the {@link PermissionModule} to load extra
     * permissions a player might have besides their group permissions
     * @param uniqueId the uuid of the player
     * @param group       the group of the player
     *
     * @see PermissionModule
     * @see PermissionGroup
     */
    public PermissionPlayer (UUID uniqueId, String group){
        this.uniqueId = uniqueId;
        this.group = group;
        this.permissions = new ArrayList<>();
    }

    /**
     * PermissionPlayer is a data class that contains all the data needed for the {@link PermissionModule} to load extra
     * permissions a player might have besides their group permissions
     * @param player the player
     * @param group       the group of the player
     *
     * @see PermissionModule
     * @see PermissionGroup
     */
    public PermissionPlayer (Player player, String group){
        this(player.getUniqueId(), group);
    }


    /**
     * Gets the permission attachment for the player, this used by Spigot to make
     * sure the {@link Player#hasPermission(String)} api works
     * This adds the player's extra permissions and group permissions to the PermissionAttachment object
     * @param player the player
     * @return the {@link PermissionAttachment} for the player
     *
     * @see PermissionAttachment
     */
    public PermissionAttachment toPermissionAttachment (Player player) {
        PermissionAttachment attachment = player.addAttachment(ScorchCore.getInstance());
        getPermissions().forEach(node -> attachment.setPermission(node, true));
        ((PermissionModule)ScorchCore.getInstance().getModule("PermissionModule"))
                .getGroup(getGroup()).getPermissions().forEach(node -> attachment.setPermission(node, true));
        return attachment;
    }

    /**
     * Adds the permission node to the player's permissions
     * @param node the node to add
     */
    public void addPermission (String node) {
        if(!getPermissions().contains(node)){
            getPermissions().add(node);
        }
    }

    /**
     * Returns true if the player has the permission node
     * @param node the node to check
     * @return     whether the player has access to the permission node
     */
    public boolean hasPermission (String node){
        return getPermissions().contains(node);
    }

    /**
     * Gets the player's UUID
     * @return the uuid
     */
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Gets the player's group
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Gets the {@link List} of permissions this player has
     * @return the {@link List} of permissions
     */
    public List<String> getPermissions() {
        return permissions;
    }
}
