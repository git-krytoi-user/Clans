package dev.r1nex.clans.utils;

import dev.r1nex.clans.data.Clan;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    private String chatColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public List<Component> lore(List<String> lore, Clan clan) {
        List<Component> components = new ArrayList<>(lore.size());
        for (String string : lore) {
            String name = ChatColor.translateAlternateColorCodes('&', clan.getName());
            Player owner = Bukkit.getPlayer(clan.getOwner());
            String ownerName = owner != null ? owner.getName() : ChatColor.RED + "OFFLINE";
            int currency = clan.getCurrency();
            int rating = clan.getRating();
            boolean pvpBoolean = clan.isPvp();
            String pvpText = pvpBoolean ? ChatColor.GREEN + "Включено" : ChatColor.RED + "Выключено";

            String lines = ChatColor.translateAlternateColorCodes('&', string
                    .replace("%clan_name%", name)
                    .replace("%clan_owner%", ownerName)
                    .replace("%clan_pvp_boolean%", pvpText)
                    .replace("%clan_currency%", String.valueOf(currency))
                    .replace("%clan_rating%", String.valueOf(rating))
            );

            components.add(Component.text(chatColor(lines)));
        }
        return components;
    }
}
