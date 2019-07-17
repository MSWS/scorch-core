package com.scorch.core.modules.permissions;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.util.*;

/**
 * A permissions handler
 * TODO Implement a way to add default groups/permissions
 */
public class PermissionModule extends AbstractModule {

	private Map<UUID, PermissionAttachment> permissionAttachments;
	private Map<UUID, PermissionPlayer> playerPermissions;
	private Collection<PermissionGroup> groupList;

	private PermissionListener permissionListener;

    private final File permissionsFile = new File(ScorchCore.getInstance().getDataFolder(), "permissions.yml");

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

		if (this.groupList == null) {
			this.groupList = new ArrayList<>();
		}

        if(this.permissionsFile.exists()){
            // Found configuration server, parse permissions.yml and update database if necessary
        }

        this.permissionListener = new PermissionListener(this);
    }

	@Override
	public void disable() {

	}

	/**
	 * Gets the {@link PermissionPlayer} for the uuid Returns null if the player
	 * isn't registered
	 * 
	 * @param uuid the uuid
	 * @return the {@link PermissionPlayer}
	 *
	 * @see PermissionPlayer
	 */
	public PermissionPlayer getPermissionPlayer(UUID uuid) {
		if (getPlayerPermissions().containsKey(uuid)) {
			return getPlayerPermissions().get(uuid);
		}
		return null;
	}

	/**
	 * Gets the {@link PermissionPlayer} for the player Returns null if the player
	 * isn't registered
	 * 
	 * @param player the player
	 * @return the {@link PermissionPlayer}
	 *
	 * @see PermissionPlayer
	 */
	public PermissionPlayer getPermissionPlayer(Player player) {
		if (getPlayerPermissions().containsKey(player.getUniqueId())) {
			return getPlayerPermissions().get(player.getUniqueId());
		}
		return null;
	}

	/**
	 * Removes the player from the {@link PermissionPlayer} map. Returns true if the
	 * player is successfully removed
	 * 
	 * @param uuid the uuid of the player
	 * @return returns true if the players has been removed successfully
	 */
	public boolean removePlayer(UUID uuid) {
		if (getPlayerPermissions().containsKey(uuid)) {
			getPlayerPermissions().remove(uuid);
			return true;
		}
		return false;
	}

	/**
	 * Adds the player to the permission list Returns true if the player is
	 * successfully added to the permission list
	 * 
	 * @param uuid             the uuid of the player
	 * @param permissionPlayer the {@link PermissionPlayer} object to add
	 * @return if the player is added
	 */
	public boolean addPlayer(UUID uuid, PermissionPlayer permissionPlayer) {
		if (!getPlayerPermissions().containsKey(uuid)) {
			getPlayerPermissions().put(uuid, permissionPlayer);
			return true;
		}
		return false;
	}

	/**
	 * Removes the player from the list
	 * 
	 * @param player the uuid of the player
	 */
	public void removePlayer(Player player) {
		if (getPlayerPermissions().containsKey(player.getUniqueId())) {
			getPlayerPermissions().remove(player.getUniqueId());
		}
	}

	/**
	 * Gets the {@link PermissionGroup} by name Returns null if the group doesn't
	 * exist
	 * 
	 * @param groupName the group's name
	 * @return the {@link PermissionGroup}
	 */
	public PermissionGroup getGroup(String groupName) {
		for (PermissionGroup group : groupList) {
			if (group.getGroupName().equalsIgnoreCase(groupName)) {
				return group;
			}
		}
		return null;
	}

	public Map<UUID, PermissionAttachment> getPermissionAttachments() {
		return permissionAttachments;
	}

	public Map<UUID, PermissionPlayer> getPlayerPermissions() {
		return playerPermissions;
	}

	public Collection<PermissionGroup> getGroupList() {
		return groupList;
	}

	public PermissionListener getPermissionListener() {
		return permissionListener;
	}
}
