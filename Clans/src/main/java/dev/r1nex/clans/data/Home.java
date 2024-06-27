package dev.r1nex.clans.data;

import org.bukkit.World;

public class Home {
    private int id;
    private final int clanId;
    private final String name;
    private final World world;
    private final double x;
    private final double y;
    private final double z;

    public Home(int id, int clanId, String name, World world, double x, double y, double z) {
        this.id = id;
        this.clanId = clanId;
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public World getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getClanId() {
        return clanId;
    }

    public String getName() {
        return name;
    }
}
