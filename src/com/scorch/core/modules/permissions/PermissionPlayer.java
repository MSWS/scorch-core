package com.scorch.core.modules.permissions;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.annotations.DataIgnore;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * PermissionPlayer is a data class that contains all the data needed for the
 * {@link PermissionModule} to load extra permissions a player might have
 * besides their groups permissions.
 *
 * @see PermissionModule
 * @see PermissionGroup
 */
public class PermissionPlayer {

	private UUID uniqueId;
	private List<String> groups;
	private List<String> permissions;

	@DataIgnore
	private PermissionAttachment attachment;

	/**
	 * Default constructor for {@link com.scorch.core.modules.data.DataManager} to
	 * work
	 */
	public PermissionPlayer() {
	}

	/**
	 * PermissionPlayer is a data class that contains all the data needed for the
	 * {@link PermissionModule} to load extra permissions a player might have
	 * besides their groups permissions
	 *
	 * @param uniqueId    the uuid of the player
	 * @param groups      the groups of the player
	 * @param permissions the permissions to add
	 *
	 * @see PermissionModule
	 * @see PermissionGroup
	 */
	public PermissionPlayer(UUID uniqueId, List<String> groups, String... permissions) {
		this.uniqueId = uniqueId;
		this.groups = groups;
		this.permissions = new ArrayList<>(Arrays.asList(permissions));
	}

	/**
	 * PermissionPlayer is a data class that contains all the data needed for the
	 * {@link PermissionModule} to load extra permissions a player might have
	 * besides their groups permissions
	 *
	 * @param player      the player
	 * @param groups      the groups of the player
	 * @param permissions the permissions to add
	 *
	 * @see PermissionModule
	 * @see PermissionGroup
	 */
	public PermissionPlayer(Player player, List<String> groups, String... permissions) {
		this(player.getUniqueId(), groups, permissions);
	}

	/**
	 * PermissionPlayer is a data class that contains all the data needed for the
	 * {@link PermissionModule} to load extra permissions a player might have
	 * besides their groups permissions
	 *
	 * @param uniqueId the uuid of the player
	 * @param groups   the groups of the player
	 *
	 * @see PermissionModule
	 * @see PermissionGroup
	 */
	public PermissionPlayer(UUID uniqueId, List<String> groups) {
		this.uniqueId = uniqueId;
		this.groups = groups;
		this.permissions = new ArrayList<>();
	}

	/**
	 * PermissionPlayer is a data class that contains all the data needed for the
	 * {@link PermissionModule} to load extra permissions a player might have
	 * besides their groups permissions
	 *
	 * @param player the player
	 * @param groups the groups of the player
	 *
	 * @see PermissionModule
	 * @see PermissionGroup
	 */
	public PermissionPlayer(Player player, List<String> groups) {
		this(player.getUniqueId(), groups);
	}

	/**
	 * Creates the permission attachment for the player, this used by Spigot to make
	 * sure the {@link Player#hasPermission(String)} api works This also adds the
	 * player's extra permissions and group permissions to the PermissionAttachment
	 * object
	 * 
	 * @param player the player
	 * @return the {@link PermissionAttachment} for the player
	 *
	 * @see PermissionAttachment
	 */
	public PermissionAttachment createAttachment(Player player) {
		// Create permission attachment object through bukkit api
		PermissionAttachment attachment = player.addAttachment(ScorchCore.getInstance());

		// Loop through the groups and add them to the attachment
		getGroups().forEach(group -> {
			addGroupPermissions(group, attachment);
		});

		// Add the player's own custom permissions
		getPermissions().forEach(node -> {
			if (!attachment.getPermissions().containsKey(node) || !attachment.getPermissions().get(node)) {
				attachment.setPermission(node, true);
			}
		});

		return attachment;
	}

	/**
	 * This method adds all the permissions of the group to the
	 * {@link PermissionAttachment} This method is required since adding all the
	 * inherited groups for each group cannot be done without recursion.
	 * 
	 * @param group      the group
	 * @param attachment the attachment to add the permissions to
	 */
	private void addGroupPermissions(PermissionGroup group, PermissionAttachment attachment) {
		// Add the permissions for the parent groups first
		group.getInheritedGroups().forEach(parent -> {
			addGroupPermissions(parent, attachment);
		});

		// Add own group's permissions
		group.getPermissions().forEach(permission -> {
			if (!attachment.getPermissions().containsKey(permission) || !attachment.getPermissions().get(permission)) {
				attachment.setPermission(permission, true);
			}
		});
	}

	/**
	 * Adds the permission node to the player's permissions
	 * 
	 * @param node the node to add
	 * @return whether the operation was successful
	 */
	public boolean addPermission(String node) {
		if (!getPermissions().contains(node)) {
			getPermissions().add(node);
			updatePermissions();
			return true;
		}
		return false;
	}

	/**
	 * Removes the permission node from the player's permissions
	 * 
	 * @param node the node to remove
	 * @return whether the operation was successful
	 */
	public boolean removePermission(String node) {
		if (getPermissions().contains("node")) {
			getPermissions().remove(node);
			updatePermissions();
			return true;
		}
		return false;
	}

	/**
	 * Adds the group and all of its permissions + inherited groups to the player
	 * 
	 * @param group the group to add
	 * @return whether the operation was successful
	 */
	public boolean addGroup(PermissionGroup group) {
		if (group == null)
			return false;
		if (!getGroupNames().contains(group.getGroupName())) {
			getGroups().add(group);
			updatePermissions();
			return true;
		}
		return false;
	}

	/**
	 * Removes the group and all of its permissions + inherited groups from the
	 * player
	 * 
	 * @param group the group to remove
	 * @return whether the operation was successful
	 */
	public boolean removeGroup(PermissionGroup group) {
		if (group == null)
			return false;
		if (getGroupNames().contains(group.getGroupName())) {
			getGroups().remove(group);
			updatePermissions();
			return true;
		}
		return false;
	}

	/**
	 * Removes the player from all groups and only adds them back to the group
	 * given.
	 * 
	 * @param group the group to set
	 * @return whether the operation was successful
	 */
	public boolean setGroup(PermissionGroup group) {
		if (group == null)
			return false;
		getGroups().clear();
		getGroups().add(group);
		updatePermissions();
		return true;
	}

	/**
	 * Updates the a permissions for the player, removes the
	 * {@link PermissionAttachment} and adds it again using the updated permissions
	 */
	public void updatePermissions() {
		ScorchCore.getInstance().getDataManager().updateObjectAsync("permissions", this,
				new SQLSelector("uniqueId", getUniqueId()));
		Player player = (Player) getAttachment().getPermissible();
		player.removeAttachment(getAttachment());
		this.createAttachment(player);
	}

	/**
	 * Gets the player's UUID.
	 *
	 * @return the uuid
	 */
	public UUID getUniqueId() {
		return uniqueId;
	}

	/**
	 * Returns true if the player is a member of the group
	 * 
	 * @param group the group to check
	 * @return whether the player is a member of the group
	 */
	public boolean hasGroup(PermissionGroup group) {
		return getGroups().contains(group);
	}

	public PermissionGroup getPrimaryGroup() {
		if (getGroups().size() > 0)
			return null;
		PermissionGroup[] permissionGroups = new PermissionGroup[getGroups().size()];
		getGroups().toArray(permissionGroups);
		Arrays.sort(permissionGroups);
		return permissionGroups[0];
	}

	/**
	 * Gets the player's groups.
	 * 
	 * @return the groups
	 */
	public List<PermissionGroup> getGroups() {
		List<PermissionGroup> permissionGroups = new ArrayList<>();
		this.groups.forEach(group -> {
			if (ScorchCore.getInstance().getPermissionModule().getGroup(group) != null) {
				permissionGroups.add(ScorchCore.getInstance().getPermissionModule().getGroup(group));
			}
		});
		return permissionGroups;
	}

	/**
	 * Returns a list of the names of the player's groups
	 * 
	 * @return the group names
	 */
	public List<String> getGroupNames() {
		return this.groups;
	}

	/**
	 * Returns true if the player is in the group
	 * 
	 * @param group the group to check
	 * @return whether the player is in the group
	 */
	@SuppressWarnings("unlikely-arg-type")
	public boolean hasGroup(String group) {
		return getGroups().contains(group);
	}

	/**
	 * Gets the player's permissions
	 * 
	 * @return
	 */
	public List<String> getPermissions() {
		return permissions;
	}

	/**
	 * Gets the player's permission attachment This is what handles the permissions
	 * on Spigot's side
	 * 
	 * @return the {@link PermissionAttachment} of the player.
	 *
	 * @see PermissionAttachment
	 */
	private PermissionAttachment getAttachment() {
		return attachment;
	}

	/**
	 * Sets the player's permission attachment This is what handles the permissions
	 * on Spigot's side.
	 * 
	 * @param attachment the new {@link PermissionAttachment} to set
	 *
	 * @see PermissionAttachment
	 */
	@SuppressWarnings("unused")
	private void setAttachment(PermissionAttachment attachment) {
		this.attachment = attachment;
	}
}