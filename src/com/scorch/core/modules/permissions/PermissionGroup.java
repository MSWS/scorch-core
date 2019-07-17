package com.scorch.core.modules.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionGroup {

	private String groupName;
	private boolean isDefault;
	private String prefix;
	private List<String> permissions;

	/**
	 * Empty constructor to accommodate for the data manager
	 */
	public PermissionGroup() {
		// Default constructor
		this.permissions = new ArrayList<>();
	}

	/**
	 * Creates a new PermissionGroup with the name, display name and permissions
	 * given
	 * 
	 * @param groupName   the name of the group (used for internal purposes)
	 * @param prefix      the display name of the group
	 * @param isDefault   whether the group is a default group
	 * @param permissions the permissions of the group
	 */
	public PermissionGroup(String groupName, boolean isDefault, String prefix, List<String> permissions) {
		this.groupName = groupName;
		this.isDefault = isDefault;
		this.prefix = prefix;
		this.permissions = permissions;
	}

	/**
	 * Creates a new PermissionGroup with the name, display name and permissions
	 * given
	 *
	 * @param groupName   the name of the group (used for internal purposes)
	 * @param prefix      the display name of the group
	 * @param isDefault   whether the group is a default group
	 * @param permissions the permissions of the group
	 */
	public PermissionGroup(String groupName, boolean isDefault, String prefix, String... permissions) {
		this.groupName = groupName;
		this.isDefault = isDefault;
		this.prefix = prefix;
		this.permissions = Arrays.asList(permissions);
	}

	/**
	 * Gets the group name
	 * 
	 * @return the group name
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Sets the group name The group name is used for identifying groups and running
	 * logic on them since, the players are only able to see the
	 * {@link PermissionGroup#prefix} in game.
	 * 
	 * @param groupName the group name to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Gets the group display name
	 * 
	 * @return the group display name
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Sets the group display name The group display name is used for chat
	 * formatting and other in game features where the player's group / "rank" is
	 * used. For running logic on groups, and saving them the
	 * {@link PermissionGroup#groupName} is used
	 * 
	 * @param prefix the group display name to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Returns true if this group has the given permission
	 * 
	 * @param permission the permission to check
	 * @return whether this group contains the permission
	 */
	public boolean hasPermission(String permission) {
		if (this.permissions == null)
			return false;
		return this.permissions.contains(permission);
	}

	/**
	 * Gets the {@link List} of permissions this group has access too, for checking
	 * if the group has access to a certain permission node please use
	 * {@link PermissionGroup#hasPermission(String)}
	 * 
	 * @see PermissionGroup#hasPermission(String)
	 * @return the {@link List} of permissions
	 */
	public List<String> getPermissions() {
		return permissions;
	}

	/**
	 * Sets whether the group is the default group players receive
	 * @apiNote only used internally for me to test stuff
	 * @param isDefault the new isDefault value
	 */
	public void setDefault (boolean isDefault){
		this.isDefault = isDefault;
	}

	public boolean isDefault() {
		return isDefault;
	}

	// No setter method for the permission list since that won't be used
}
