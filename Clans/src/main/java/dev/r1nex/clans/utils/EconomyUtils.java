package dev.r1nex.clans.utils;

import dev.r1nex.clans.Clans;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyUtils {
    private final Clans plugin;
    private Economy economy;

    public EconomyUtils(Clans plugin) {
        this.plugin = plugin;
        if (!setupEconomy()) {
            plugin.getLogger().severe("[%s] - Disabled due to no Vault dependency found!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public boolean takeMoney(Player player, double money) {
        if (economy.getBalance(player) >= money) {
            economy.withdrawPlayer(player, money).transactionSuccess();
            return true;
        } else return false;
    }
}
