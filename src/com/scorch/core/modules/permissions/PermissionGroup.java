package com.scorch.core.modules.permissions;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.annotations.DataPrimaryKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionGroup implements Comparable<PermissionGroup> {

	@DataPrimaryKey
	private String groupName;
	private boolean isDefault;
	private String prefix;
	private int weight;
	private List<String> inheritedGroups;
	private List<String> permissions;

	/**
	 * Default constructor for {@link com.scorch.core.modules.data.DataManager} to work
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
	 * @param isDefault   whether the group is a default group
	 * @param prefix      the display name of the group
	 * @param weight      the weight of the group
	 * @param permissions the permissions of the group
	 */
	public PermissionGroup(String groupName, boolean isDefault, String prefix, int weight, List<String> permissions) {
		this.groupName = groupName;
		this.isDefault = isDefault;
		this.prefix = prefix;
		this.weight = weight;
		this.permissions = permissions;
	}

	/**
	 * Creates a new PermissionGroup with the name, display name and permissions
	 * given
	 *
	 * @param groupName   the name of the group (used for internal purposes)
	 * @param isDefault   whether the group is a default group
	 * @param prefix      the display name of the group
	 * @param weight      the weight of the group
	 * @param permissions the permissions of the group
	 */
	public PermissionGroup(String groupName, boolean isDefault, String prefix, int weight, String... permissions) {
		this(groupName, isDefault, prefix, weight, Arrays.asList(permissions));
	}

	/**
	 * Creates a new PermissionGroup with the name, display name and permissions
	 * given
	 *
	 * @param groupName       the name of the group (used for internal purposes)
	 * @param isDefault       whether the group is a default group
	 * @param prefix          the display name of the group
	 * @param weight          the weight of the group
	 * @param inheritedGroups the groups this group inherits
	 * @param permissions     the permissions of the group
	 */
	public PermissionGroup (String groupName, boolean isDefault, String prefix, int weight, List<String> inheritedGroups, List<String> permissions) {
		this.groupName = groupName;
		this.isDefault = isDefault;
		this.prefix = prefix;
		this.weight = weight;
		this.inheritedGroups = inheritedGroups;
		this.permissions = permissions;
	}

	/**
	 * Creates a new PermissionGroup with the name, display name and permissions
	 * given
	 *
	 * @param groupName       the name of the group (used for internal purposes)
	 * @param isDefault       whether the group is a default group
	 * @param prefix          the display name of the group
	 * @param weight          the weight of the group
	 * @param inheritedGroups the groups this group inherits
	 * @param permissions     the permissions of the group
	 */
	public PermissionGroup (String groupName, boolean isDefault, String prefix, int weight, List<String> inheritedGroups, String... permissions) {
		this(groupName, isDefault, prefix, weight, inheritedGroups, Arrays.asList(permissions));
	}

	/**
	 * Gets the group name <br>The group name is used for identifying groups and running
	 * logic on them since, the players are only able to see the
	 * {@link PermissionGroup#prefix} in game.</br>
	 * @return the group name
	 *
	 * @see PermissionGroup#getGroupName()
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Sets the group name <br>The group name is used for identifying groups and running
	 * logic on them since, the players are only able to see the
	 * {@link PermissionGroup#prefix} in game.</br>
	 * 
	 * @param groupName the group name to set
	 *
	 * @see PermissionGroup#setGroupName(String)
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Gets the group prefix, the group prefix is used for chat
	 * formatting and other in game features where the player's group / "rank" is
	 * used. For running logic on groups, and saving them the
	 * {@link PermissionGroup#groupName} is used
	 * @return the group prefix
	 *
	 * @see PermissionGroup#getGroupName()
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Sets the group prefix, the group prefix is used for chat
	 * formatting and other in game features where the player's group / "rank" is
	 * used. For running logic on groups, and saving them the
	 * {@link PermissionGroup#groupName} is used
	 * 
	 * @param prefix the group display name to set
	 *
	 * @see PermissionGroup#setGroupName(String)
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix.replace("_", " ");
		ScorchCore.getInstance().getDataManager().updateObjectAsync("groups", this);
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


	/**
	 * Gets the groups this group inherits
	 * Inherited groups are basically parent groups, as this group has all their permissions as well.
	 * @return a list of all the {@link PermissionGroup}s this group inherits
	 */
	public List<PermissionGroup> getInheritedGroups() {
		if(this.inheritedGroups == null) this.inheritedGroups = new ArrayList<String>();
		List<PermissionGroup> groups = new ArrayList<>();
		this.inheritedGroups.forEach(group -> {
			if(ScorchCore.getInstance().getPermissionModule().getGroup(group) != null){
				groups.add(ScorchCore.getInstance().getPermissionModule().getGroup(group));
			}
		});
		return groups;
	}

	/**
	 * Gets the names of the groups this group inherits
	 * Inherited groups are basically parent groups, as this group has all their permissions as well.
	 * @return a list of all the group names of the inherited groups for this group
	 */
	public List<String> getInheritedGroupNames () {
		return this.inheritedGroups;
	}

	/**
	 * Sets the gorups this groups inherits
	 * Inherited groups are basically parent groups, as this group has all their permissions as well.
	 * @param inheritedGroups the new groups this group will inherit
	 */
	public void setInheritedGroups(List<String> inheritedGroups) {
		this.inheritedGroups = inheritedGroups;
	}

	/**
	 * Adds the group to the inherited group list, if it's not already there
	 * Returns true if the group was added successfully, false if not
	 * @param group the group to add to the inherited group list
	 *
	 * @returns     whether the group was added successfully
	 */
	public boolean addInheritedGroup (PermissionGroup group){
		if(!getInheritedGroups().contains(group)){
			// Make sure to add it to the string list and not the getInheritedGroups list since that's a different list
			this.inheritedGroups.add(group.getGroupName());
			return true;
		}
		return false;
	}

	public boolean removeInheritedGroup (PermissionGroup group){
		if(getInheritedGroups().contains(group)){
			// Make sure to add it to the string list and not the getInheritedGroups list since that's a different list
			this.inheritedGroups.remove(group.getGroupName());
			return true;
		}
		return false;
	}

	/**
	 * Adds the permission to the group
	 * @param node the permission to add
	 * @return     whether the operation was successful
	 */
	public boolean addPermission(String node){
		if(node == "" || node == null) return false;
		if(!getPermissions().contains(node)){
			getPermissions().add(node);
			ScorchCore.getInstance().getDataManager().updateObjectAsync("groups", this);
			return true;
		}
		return false;
	}

	/**
	 * Removes the permission from the group
	 * @param node the permission to remove
	 * @return     whether the operation was successful
	 */
	public boolean removePermission (String node){
		if(node == "" || node == null) return false;
		if(getPermissions().contains(node)){
			getPermissions().remove(node);
			ScorchCore.getInstance().getDataManager().updateObjectAsync("groups", this);
			return true;
		}
		return false;
	}

	/**
	 * Gets this group's weight, the weight determines whether the prefix is used or not
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Sets the group's weight, the weight determines whether the prefix is used or not
	 * @param weight the new weight to use
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}


	/**
	 * Compares the given group to this group instance
	 * @param object the group you want to compare this group to
	 * @return      whether these two groups are identical
	 */
	@Override
	public boolean equals (Object object){
		if(!(object instanceof PermissionGroup)) {
			return false;
		}
		PermissionGroup group = (PermissionGroup) object;

		if(group.getPermissions().size() != getPermissions().size()) return false;
		if(group.getInheritedGroupNames().size() != getInheritedGroupNames().size()) return false;

		for(String perm : getPermissions()){
			if(!group.getPermissions().contains(perm)) {
				return false;
			}

		}

		return (getGroupName().equals(group.getGroupName()) && getPrefix().equals(group.getPrefix())
				&& getWeight() == group.getWeight() && isDefault() == group.isDefault());
	}

	/**
	 * Returns {@link PermissionGroup#groupName}
	 * @return the group name
	 */
	@Override
	public String toString () {
		return getGroupName();
	}

	@Override
	public int compareTo(PermissionGroup o) {
		return o.getWeight() - this.getWeight();
	}
}
