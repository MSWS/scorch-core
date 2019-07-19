package com.scorch.core.modules.permissions;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.data.exceptions.DataDeleteException;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.DataUpdateException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
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
	// Changed to List from Collection since i need List#get(int)
	private List<PermissionGroup> groupList;

	private PermissionListener permissionListener;

    private final File permissionsFile = new File(ScorchCore.getInstance().getDataFolder(), "permissions.yml");

    public PermissionModule(String id) {
        super(id);
    }

	@Override
	public void initialize() {

    	Logger.info("&6Initialising permissions...");

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
		} catch (NoDefaultConstructorException | DataObtainException e) {
			e.printStackTrace();
		}

		if (this.groupList == null) {
			this.groupList = new ArrayList<>();
		}

		Logger.log("&6Loaded %s groups from database", groupList.size());

		// Checking if permissions.yml file exists
        if(this.permissionsFile.exists()){
            // Found configuration server, parse permissions.yml and update database if necessary
			Logger.log("&6Found permissions.yml file, checking for changes...");
			YamlConfiguration permissions = YamlConfiguration.loadConfiguration(permissionsFile);
			for(String group : permissions.getConfigurationSection("groups").getKeys(false)){
				// Load group from config
				Logger.log("&6   - Parsing &e%s", group);
				String groupPath = "groups." + group;
				List<String> inherits = permissions.getStringList(groupPath + ".inherits");
				boolean isDefault = permissions.getBoolean(groupPath + ".default");
				String prefix = permissions.getString(groupPath + ".prefix");
				int weight = permissions.getInt(groupPath + ".weight");
				List<String> permissionList = permissions.getStringList(groupPath + ".permissions");

				PermissionGroup groupObject = new PermissionGroup(group, isDefault, prefix, weight, inherits, permissionList);
				ymlGroups.add(groupObject);
				Logger.log("&a       SUCCESS");
			}
        }


        if(groupList == null || groupList.isEmpty()){
        	// database is empty, save ymlGroups
			Logger.log("&6Database was empty, saving all groups to database...");
			groupList = new ArrayList<>(ymlGroups);
			groupList.forEach(group -> {
				ScorchCore.getInstance().getDataManager().saveObject("groups", group);
			});
			Logger.log("&6Done!");
		}
        else {
			Logger.log("&6Checking for any required updates to database...");
			for (int i = 0; i < groupList.size(); i++) {
				PermissionGroup group = groupList.get(i);
				Logger.log("&6   - Checking &e%s", group.getGroupName());
				for (PermissionGroup ymlGroup : ymlGroups) {
					if(group.getGroupName().equals(ymlGroup.getGroupName())){
						// group exists
						if (!group.equals(ymlGroup)) {
							Logger.log("      &eCHANGED");
							try {
								ScorchCore.getInstance().getDataManager().updateObject("groups", ymlGroup, new SQLSelector("groupName", ymlGroup.getGroupName()));
							} catch (DataUpdateException e) {
								e.printStackTrace();
							}
							groupList.set(i, ymlGroup);
						}
						else {
							Logger.log("      &aNO CHANGES");
						}
					}
				}
			}

			Logger.log("&6Checking for new groups...");
			for (PermissionGroup ymlGroup : ymlGroups) {
				boolean exists = false;
				for(int i = 0; i < groupList.size(); i++){
					PermissionGroup group = groupList.get(i);
					if(group.getGroupName().equals(ymlGroup.getGroupName())){
						// Group exists
						exists = true;
						break;
					}
				}
				if(!exists){
					groupList.add(ymlGroup);
					ScorchCore.getInstance().getDataManager().saveObject("groups", ymlGroup);
					Logger.log("   - &6New group: &e%s", ymlGroup.getGroupName());
				}
			}

			Logger.log("&6Checking if there's any groups to delete...");
			for(int i = 0; i < groupList.size(); i++){
				PermissionGroup group = groupList.get(i);
				if(!ymlGroups.contains(group)){
					Logger.log("&6   - &e%s &6doesn't exist in .yml file anymore, deleting", group.getGroupName());
					try {
						ScorchCore.getInstance().getDataManager().deleteObject("groups", new SQLSelector("groupName", group.getGroupName()));
						Logger.log("&a       SUCCESS");
					} catch (DataDeleteException e) {
						e.printStackTrace();
					}
				}
			}
		}

		Logger.log("&6Done!");

        this.permissionListener = new PermissionListener(this);
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
			if (group.getGroupName().equals(groupName)) {
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
