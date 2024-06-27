package dev.r1nex.clans.data;

public class Role {
    private final int clanId;
    private int id;
    private String name;

    public Role(int clanId, int id, String name) {
        this.clanId = clanId;
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClanId() {
        return clanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
