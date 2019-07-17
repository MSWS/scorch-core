package com.scorch.core.modules.data.wrappers;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * A simple wrapper for the {@link Location} to make sure Gson can serialise it.
 */
public class JSONLocation {

    private String world;
    private double x, y, z;
    private float pitch, yaw;

    public JSONLocation(String world, double x, double y, double z, float pitch, float yaw) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    /**
     * Returns the {@link Location} that this object is representing
     * @return the location
     */
    public Location toBukkitLocation (){
        return new Location(Bukkit.getWorld(this.getWorld()), this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
    }

    /**
     * Creates a new JSONLocation from an existing {@link org.bukkit.Location}
     * @param location the location to convert
     * @return         the JSONLocation
     */
    public static JSONLocation fromLocation (Location location){
        return new JSONLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                location.getPitch(), location.getYaw());
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
