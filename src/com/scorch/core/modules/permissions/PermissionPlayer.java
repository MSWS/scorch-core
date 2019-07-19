package com.scorch.core.modules.permissions;

import com.scorch.core.ScorchCore;
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
	 * object <br>
	 * <b>THIS SHOULD ONLY BE CALLED ONCE!</b></br>
	 * 
	 * @param player the player
	 * @return the {@link PermissionAttachment} for the player
	 *
	 * @see PermissionAttachment
	 */
	public PermissionAttachment createAttachment(Player player) {
		PermissionAttachment attachment = player.addAttachment(ScorchCore.getInstance());
		getPermissions().forEach(node -> attachment.setPermission(node, true));
		getGroups().forEach(group -> {
			((PermissionModule) ScorchCore.getInstance().getModule("PermissionModule")).getGroup(group).getPermissions()
					.forEach(node -> attachment.setPermission(node, true));
		});
		return attachment;
	}

	/**
	 * Adds the permission node to the player's permissions
	 * 
	 * @param node the node to add
	 */
	public void addPermission(String node) {
		if (!getPermissions().contains(node)) {
			getPermissions().add(node);
			updatePermissions();
		}
	}

	/**
	 * Updates the a permissions for the player, removes the
	 * {@link PermissionAttachment} and adds it again using the updated permissions
	 */
	public void updatePermissions() {
		Player player = (Player) getAttachment().getPermissible();
		player.removeAttachment(getAttachment());
		createAttachment(player);
	}

	/**
	 * Returns true if the player has the permission node.
	 * 
	 * @param node the node to check
	 * @return whether the player has access to the permission node
	 */
	public boolean hasPermission(String node) {
		return getPermissions().contains(node);
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
	 * Gets the player's groups.
	 * 
	 * @return the groups
	 */
	public List<String> getGroups() {
		return groups;
	}

	/**
	 * Returns true if the player is in the group
	 * 
	 * @param group the group to check
	 * @return whether the player is in the group
	 */
	public boolean hasGroup(String group) {
		return getGroups().contains(group);
	}

	/**
	 * Gets the {@link List} of permissions this player has.
	 * 
	 * @return the {@link List} of permissions
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
	public PermissionAttachment getAttachment() {
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
	public void setAttachment(PermissionAttachment attachment) {
		this.attachment = attachment;
	}
}
