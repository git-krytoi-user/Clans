package dev.r1nex.clans.data;

import java.util.List;
import java.util.UUID;

public class Clan {
    private int id;
    private final UUID owner;
    private final String name;
    private int currency;
    private int rating;
    private List<Member> members;
    private final List<Role> roles;
    private final List<Home> homes;
    private boolean pvp;

    public Clan(int id, UUID owner, String name, int currency, int rating, List<Member> members, List<Role> roles, List<Home> homes) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.currency = currency;
        this.rating = rating;
        this.members = members;
        this.roles = roles;
        this.homes = homes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Home> getHomes() {
        return homes;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public int getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public int getCurrency() {
        return currency;
    }

    public int getRating() {
        return rating;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
}
