package dev.r1nex.clans;

import dev.r1nex.clans.commands.MainCommand;
import dev.r1nex.clans.config.DefaultConfig;
import dev.r1nex.clans.data.Clan;
import dev.r1nex.clans.database.MySQL;
import dev.r1nex.clans.listeners.Listeners;
import dev.r1nex.clans.utils.ClanUtil;
import dev.r1nex.clans.utils.EconomyUtils;
import dev.r1nex.clans.utils.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Clans extends JavaPlugin {

    private DefaultConfig defaultConfig;
    private MySQL mySQL;
    private List<Clan> clans;
    public final int cooldown = 5;
    private final List<Player> cooldowns = new ArrayList<>();
    private final HashMap<UUID, Clan> playerClanInvites = new HashMap<>();
    private final List<UUID> playersLeaves = new ArrayList<>();
    private ItemUtil itemUtil;
    private ClanUtil clanUtil;
    private EconomyUtils economyUtils;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        mySQL = new MySQL(this,
                "77.222.37.34",
                3306,
                "s1_anarchy",
                "u1_gRBBDnBuqh",
                "8al=WU4K5+AEeXDf=.KYfb=2"
        );
        mySQL.createTables();
        clans = mySQL.getAllClans();
        defaultConfig = new DefaultConfig(this);
        new MainCommand(this);
        itemUtil = new ItemUtil();
        clanUtil = new ClanUtil(this);
        economyUtils = new EconomyUtils(this);
    }

    public EconomyUtils getEconomyUtils() {
        return economyUtils;
    }

    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public List<Clan> getClans() {
        return clans;
    }

    public ItemUtil getItemUtil() {
        return itemUtil;
    }

    public ClanUtil getClanUtil() {
        return clanUtil;
    }

    public List<Player> getCooldowns() {
        return cooldowns;
    }

    public HashMap<UUID, Clan> getPlayerClanInvites() {
        return playerClanInvites;
    }

    public List<UUID> getPlayersLeaves() {
        return playersLeaves;
    }
}
