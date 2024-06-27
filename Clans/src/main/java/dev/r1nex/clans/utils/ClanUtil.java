package dev.r1nex.clans.utils;

import dev.r1nex.clans.Clans;
import dev.r1nex.clans.data.Clan;
import dev.r1nex.clans.data.Member;
import dev.r1nex.clans.data.Role;

import java.util.UUID;

public class ClanUtil {

    private final Clans plugin;

    public ClanUtil(Clans plugin) {
        this.plugin = plugin;
    }

    public boolean isPlayerDeputyClan(UUID uuid, Clan clan) {
        for (Member member : clan.getMembers()) {
            if (member.getMember().equals(uuid) && member.getRole() == 11) return true;
        }

        return false;
    }

    public Clan searchPlayerDeputyClan(UUID uuid) {
        for (Clan clan : plugin.getClans()) {
            for (Member member : clan.getMembers()) {
                if (member.getMember().equals(uuid) && member.getRole() == 11) return clan;
            }
        }
        return null;
    }

    public Clan searchPlayerClan(UUID uuid) {
        for (Clan clan : plugin.getClans()) {
            for (Member member : clan.getMembers()) {
                if (member.getMember().equals(uuid)) return clan;
            }
        }
        return null;
    }

    public Role getRoleByMember(UUID uuid) {
        for (Clan clan : plugin.getClans()) {
            for (Member member : clan.getMembers()) {
                if (member.getMember().equals(uuid)) {
                    if (!(member.getRole() >= 0 && member.getRole() < clan.getRoles().size())) return null;
                    return clan.getRoles().get(member.getRole());
                }
            }
        }
        return null;
    }

    public Member getMemberByUUID(UUID uuid, Clan clan) {
        for (Member member : clan.getMembers()) {
            if (member.getMember().equals(uuid)) return member;
        }
        return null;
    }

    public boolean isPlayerInClan(UUID uuid) {
        for (Clan clan : plugin.getClans()) {
            for (Member member : clan.getMembers()) {
                if (member.getMember().equals(uuid)) return true;
            }
        }
        return false;
    }
}
