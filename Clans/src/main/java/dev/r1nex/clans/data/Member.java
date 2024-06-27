package dev.r1nex.clans.data;

import java.util.UUID;

public class Member {
    private int id;
    private int clanId;
    private final UUID member;
    private int role;

    public Member(int id, int clanId, UUID member, int role) {
        this.clanId = clanId;
        this.member = member;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setClanId(int clanId) {
        this.clanId = clanId;
    }

    public int getClanId() {
        return clanId;
    }

    public UUID getMember() {
        return member;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
