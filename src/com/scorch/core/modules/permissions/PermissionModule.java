package com.scorch.core.modules.permissions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.scorch.core.modules.data.exceptions.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.utils.Logger;

/**
 * A permission handler for ScorchGamez this this will handle adding permissions to players using groups and custom perms
 * TODO: Sync permission updates across network using events
 */
public class PermissionModule extends AbstractModule {

	private Map<UUID, PermissionAttachment> permissionAttachments;
	private Map<UUID, PermissionPlayer> playerPermissions;
	// Changed to List from Collection since i need List#get(int)
	private List<PermissionGroup> groupList;

	private PermissionListener permissionListener;

	private final File permissionsFile = new File(ScorchCore.getInstance().getDataFolder(), "permissions.yml");

	public PermissionModule(String id) {
		super(id);
	}

	@Override
	public void initialize() {

		Logger.info("&9Setting up permissions...");

		// Order doesn't really matter so using HashMap
		this.permissionAttachments = new HashMap<>();
		this.playerPermissions = new HashMap<>();
		List<PermissionGroup> ymlGroups = new ArrayList<>();

		// Setup database stuff
		try {
			ScorchCore.getInstance().getDataManager().createTable("groups", PermissionGroup.class);
			ScorchCore.getInstance().getDataManager().createTable("permissions", PermissionPlayer.class);

			ScorchCore.getInstance().getDataManager().getAllObjects("permissions").forEach(obj -> {
				PermissionPlayer player = (PermissionPlayer) obj;
				playerPermissions.put(player.getUniqueId(), player);
			});

			this.groupList = new ArrayList<>(ScorchCore.getInstance().getDataManager().getAllObjects("groups"));
		} catch (NoDefaultConstructorException | DataObtainException | DataPrimaryKeyException e) {
			e.printStackTrace();
		}

		if (this.groupList == null) {
			this.groupList = new ArrayList<>();
		}

		Logger.log("&aLoaded &e%s &agroup" + (groupList.size() == 1 ? "" : "s") + " from database", groupList.size());

		// Checking if permissions.yml file exists
		if (this.permissionsFile.exists()) {
			// Found configuration server, parse permissions.yml and update database if
			// necessary
			Logger.log("&6Found permissions.yml file, checking for changes...");
			YamlConfiguration permissions = YamlConfiguration.loadConfiguration(permissionsFile);
			for (String group : permissions.getConfigurationSection("groups").getKeys(false)) {
				// Load group from config
				String groupPath = "groups." + group;
				List<String> inherits = permissions.getStringList(groupPath + ".inherits");
				boolean isDefault = permissions.getBoolean(groupPath + ".default");
				String prefix = permissions.getString(groupPath + ".prefix");
				int weight = permissions.getInt(groupPath + ".weight");
				List<String> permissionList = permissions.getStringList(groupPath + ".permissions");

				PermissionGroup groupObject = new PermissionGroup(group, isDefault, prefix, weight, inherits,
						permissionList);
				ymlGroups.add(groupObject);
				Logger.log("&a -> Parsed &e%s", group);
			}
		}

		if (groupList == null || groupList.isEmpty()) {
			// database is empty, save ymlGroups
			Logger.log("&3Database empty, &asaving current groups to database...");
			groupList = new ArrayList<>(ymlGroups);
			groupList.forEach(group -> {
				ScorchCore.getInstance().getDataManager().saveObject("groups", group);
			});
			Logger.log("&aSuccessfully saved all groups to database.");
		} else {
			Logger.log("&9Checking for database updates...");
			for (int i = 0; i < groupList.size(); i++) {
				PermissionGroup group = groupList.get(i);
				Logger.log("&9Checking &e%s &9for any updates", group.getGroupName());
				for (PermissionGroup ymlGroup : ymlGroups) {
					if (group.getGroupName().equals(ymlGroup.getGroupName())) {
						// group exists
						if (!group.equals(ymlGroup)) {
							Logger.log("&6* &cCHANGED");
							try {
								ScorchCore.getInstance().getDataManager().updateObject("groups", ymlGroup);
							} catch (DataUpdateException e) {
								e.printStackTrace();
							}
							groupList.set(i, ymlGroup);
						} else {
							Logger.log("&2* &aNO CHANGES");
						}
					}
				}
			}

			Logger.log("&9Checking for new groups...");
			for (PermissionGroup ymlGroup : ymlGroups) {
				boolean exists = false;
				for (int i = 0; i < groupList.size(); i++) {
					PermissionGroup group = groupList.get(i);
					if (group.getGroupName().equals(ymlGroup.getGroupName())) {
						// Group exists
						exists = true;
						break;
					}
				}
				if (!exists) {
					groupList.add(ymlGroup);
					ScorchCore.getInstance().getDataManager().saveObject("groups", ymlGroup);
					Logger.log("   - &6New group: &e%s", ymlGroup.getGroupName());
				}
			}

			Logger.log("&6Checking if there's any groups to delete...");
			for (int i = 0; i < groupList.size(); i++) {
				PermissionGroup group = groupList.get(i);
				if (!ymlGroups.contains(group)) {
					Logger.log("&6   - &e%s &6doesn't exist in .yml file anymore, deleting", group.getGroupName());
					try {
						ScorchCore.getInstance().getDataManager().deleteObject("groups",
								new SQLSelector("groupName", group.getGroupName()));
						Logger.log("&a       SUCCESS");
					} catch (DataDeleteException e) {
						e.printStackTrace();
					}
				}
			}
		}

		Logger.log("&aSuccessfully finished permission configuration.");

		this.permissionListener = new PermissionListener(this);

		for(Player p : Bukkit.getOnlinePlayers()){
			PermissionPlayer player = new PermissionPlayer(p.getUniqueId(), new ArrayList<>());
			this.addPlayer(p.getUniqueId(), player);
		}
	}

	@Override
	public void disable() {
		// Nothing to do really...
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
		Logger.error("returning null permission player!");
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
		if (!getPlayerPermissions().containsKey(player.getUniqueId())) {
			PermissionPlayer permissionPlayer = new PermissionPlayer(player.getUniqueId(), new ArrayList<>());
			addPlayer(player.getUniqueId(), permissionPlayer);
		}
		return getPlayerPermissions().get(player.getUniqueId());
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
			if(!permissionPlayer.hasGroup(getDefaultGroup().getGroupName())){
				permissionPlayer.addGroup(getDefaultGroup());
			}

			if(Bukkit.getPlayer(uuid) != null){
				permissionPlayer.createAttachment(Bukkit.getPlayer(uuid));
			}

			getPlayerPermissions().put(uuid, permissionPlayer);
			permissionPlayer.updatePermissions();
			return true;
		}
		return false;
	}

	/**
	 * Adds a group to the grouplist and database
	 * @param group the group to add
	 * @return      whether the group was added
	 */
	public boolean addGroup(PermissionGroup group){
		if(group == null) return false;
		if(!this.groupList.contains(group)){
			groupList.add(group);
			ScorchCore.getInstance().getDataManager().saveObjectAsync("groups", group);
			return true;
		}
		return false;
	}

	/**
	 * Deletes the group from the grouplist and database, this is a permanent action!
	 * @param groupName the group to delete
	 * @return          whether the group was deleted
	 */
	public boolean removeGroup(String groupName){
		PermissionGroup group = getGroup(groupName);
		if(group == null) return false;
		if(this.groupList.contains(group)){
			groupList.remove(group);
			ScorchCore.getInstance().getDataManager().deleteObjectAsync("groups", new SQLSelector("groupName", group.getGroupName()));
			return true;
		}
		return false;
	}

	/**
	 * Removes the player from the list
	 * <br><strong>In theory this should never be used!</strong></br>
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
			if (group.getGroupName().equals(groupName)) {
				return group;
			}
		}
		return null;
	}

	/**
	 * Get the default permission group that all players should be a part of
	 * @return the default group
	 */
	public PermissionGroup getDefaultGroup () {
		for(PermissionGroup group : groupList){
			if(group.isDefault()) {
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
