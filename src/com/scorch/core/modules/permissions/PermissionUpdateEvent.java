package com.scorch.core.modules.permissions;

import com.scorch.core.modules.communication.NetworkEvent;

/**
 * Event that's called when an update to the permission module is made, and the servers need to update
 */
public class PermissionUpdateEvent extends NetworkEvent {

    private String groupToUpdate;

    public PermissionUpdateEvent(String groupToUpdate) {
        this.groupToUpdate = groupToUpdate;
    }

    public String getGroupToUpdate() {
        return groupToUpdate;
    }

    public void setGroupToUpdate(String groupToUpdate) {
        this.groupToUpdate = groupToUpdate;
    }
}
