package dev.r1nex.clans.commands;

import dev.r1nex.clans.Clans;
import dev.r1nex.clans.data.*;
import dev.r1nex.clans.gui.Gui;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainCommand extends AbstractCommand {

    private final Clans plugin;

    public MainCommand(Clans plugin) {
        super(plugin, "clan");
        this.plugin = plugin;
    }

    private String chatColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            Gui gui = new Gui(plugin, 6, "&7Список кланов");
            gui.openAllClans(player);
            return;
        }

        if (plugin.getCooldowns().contains(player)) {
            player.sendMessage(chatColor("&cНельзя так часто использовать команду."));
            return;
        }

        plugin.getCooldowns().add(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                plugin.getCooldowns().remove(player), 20L * plugin.cooldown
        );

        switch (args[0]) {
            case "help": {
                List<String> message = plugin.getDefaultConfig().getStringList("commands-format");
                message.forEach(string -> sender.sendMessage(chatColor(string)));
                return;
            }

            case "create": {
                if (plugin.getClanUtil().isPlayerInClan(player.getUniqueId())) {
                    player.sendMessage(chatColor("&6Вы состоите в каком-то клане, и не можете создать себе клан."));
                    return;
                }

                if (args.length != 2) {
                    player.sendMessage(chatColor("&7/clan create <название> - Создать клан."));
                    return;
                }

                String clanName = args[1];
                if (clanName.length() < plugin.getDefaultConfig().getInt("settings-server.min-clan-name")) {
                    player.sendMessage(chatColor(
                            "&6Минимальное кол-во символов не < " +
                                    plugin.getDefaultConfig().getInt("settings-server.min-clan-name"))
                    );
                    return;
                }

                if (clanName.length() > plugin.getDefaultConfig().getInt("settings-server.max-clan-name")) {
                    player.sendMessage(chatColor(
                            "&6Максимальное кол-во символов не > " +
                                    plugin.getDefaultConfig().getInt("settings-server.max-clan-name"))
                    );
                    return;
                }

                Clan clan = new Clan(
                        0, player.getUniqueId(), clanName,
                        0, 0, new ArrayList<>(),
                        new ArrayList<>(), new ArrayList<>()
                );
                Gui gui = new Gui(plugin, 6, chatColor("&6Создание гильдии."));
                gui.acceptClanCreate(player, clan);
                return;
            }

            case "invite": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerDeputyClan(uuid);
                if (clan == null) {
                    player.sendMessage(chatColor("&6Вы не состоите в клане, в котором Вы можете быть заместителем."));
                    return;
                }

                if (clan.getMembers().size() >=
                        plugin.getDefaultConfig().getInt("settings-server.max-members-clan")) {
                    player.sendMessage(chatColor("&6Клан достиг максимального кол-ва участников."));
                    return;
                }

                if (args.length != 2) {
                    player.sendMessage(chatColor("&7/clan invite <игрок> - Пригласить игрока в клан."));
                    return;
                }

                String playerName = args[1];
                Player target = Bukkit.getPlayerExact(playerName);
                if (target == null) return;
                UUID uuidTarget = target.getUniqueId();
                if (plugin.getClanUtil().isPlayerInClan(uuidTarget)) return;
                if (target == player) return;

                List<String> inviteMessage = plugin.getDefaultConfig().getStringList("invite-message");
                for (String s : inviteMessage) {
                    String replace = s
                            .replace("%clan_name%", clan.getName())
                            .replace("%clan_currency%", String.valueOf(clan.getCurrency()))
                            .replace("%clan_rating%", String.valueOf(clan.getRating()));
                    plugin.getPlayerClanInvites().put(uuidTarget, clan);
                    TextComponent message = new TextComponent(chatColor(replace));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept_clan_invite"));
                    target.sendMessage(message);
                }

                player.sendMessage(chatColor("&aОжидайте ответа от игрока."));
                return;
            }

            case "exclude": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerDeputyClan(uuid);
                if (clan == null) {
                    player.sendMessage(chatColor("&6Вы не состоите в клане, в котором Вы можете быть заместителем."));
                    return;
                }

                if (args.length != 2) {
                    player.sendMessage(chatColor("&7/clan exclude <игрок> - Исключить игрока из клана."));
                    return;
                }

                String playerName = args[1];
                Player target = Bukkit.getPlayerExact(playerName);
                if (target == null) return;
                if (target == player) return;
                UUID uuidTarget = target.getUniqueId();
                Member member = plugin.getClanUtil().getMemberByUUID(uuidTarget, clan);
                if (member == null) {
                    player.sendMessage(chatColor("&cЭтот игрок не состоит в Вашем клане!"));
                    return;
                }

                boolean isSuccess = plugin.getMySQL().removeMember(
                        clan, member, "DELETE FROM clan_members WHERE clan_id = ? AND uuid = ?"
                );

                if (!isSuccess) {
                    player.sendMessage(chatColor("&cУчастник не удалился. Сообщите разработчику!"));
                    return;
                }

                String line = chatColor(plugin.getDefaultConfig().getString("messages.remove-member")
                        .replace("%string_value%", target.getName())
                );
                player.sendMessage(line);
                return;
            }

            case "role-create": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerDeputyClan(uuid);
                if (clan == null) {
                    player.sendMessage(chatColor("&6Вы не состоите в клане, в котором Вы можете быть заместителем."));
                    return;
                }

                if (clan.getRoles().size() >= plugin.getDefaultConfig().getInt("settings-server.max-clan-roles")) {
                    player.sendMessage(chatColor("&6Клан достиг максимального кол-ва ролей."));
                    return;
                }

                if (args.length != 2) {
                    player.sendMessage(chatColor("&7/clan role-create <название> - Создать роль."));
                    return;
                }

                String roleName = args[1];
                Role role = new Role(clan.getId(), 0, roleName);
                boolean isSuccess = plugin.getMySQL().addRole(
                        clan, role, "INSERT INTO roles (clan_id, name) VALUES (?, ?)"
                );

                if (!isSuccess) {
                    player.sendMessage(chatColor("&cРоль не создалась. Сообщите разработчику!"));
                    return;
                }

                String line = chatColor(plugin.getDefaultConfig().getString("messages.role-add")
                        .replace("%string_value%", roleName)
                );
                player.sendMessage(line);
                return;
            }

            case "role-remove": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerDeputyClan(uuid);
                if (clan == null) {
                    player.sendMessage(chatColor("&6Вы не состоите в клане, в котором Вы можете быть заместителем."));
                    return;
                }

                if (args.length != 2) {
                    player.sendMessage(chatColor("&7/clan role-remove <id> - Удалить роль."));
                    player.sendMessage(chatColor("&fID роли можно посмотреть в списке ролей Вашего клана."));
                    return;
                }

                int id = Integer.parseInt(args[1]);
                if (!(id >= 0 && id < clan.getRoles().size())) {
                    player.sendMessage(chatColor("&cРоль не найдена."));
                    player.sendMessage(chatColor("&fID роли можно посмотреть в списке ролей Вашего клана."));
                    return;
                }

                Role role = clan.getRoles().get(id);
                String roleName = role.getName();
                boolean isSuccess = plugin.getMySQL().removeRole(
                        clan, role, "DELETE FROM roles WHERE id = ? AND clan_id = ?"
                );

                if (!isSuccess) {
                    player.sendMessage(chatColor("&cРоль не удалена. Сообщите разработчику!"));
                    return;
                }

                String line = chatColor(plugin.getDefaultConfig().getString("messages.role-remove")
                        .replace("%string_value%", roleName)
                );
                player.sendMessage(line);
                return;
            }

            case "edit-role": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerDeputyClan(uuid);
                if (clan == null) {
                    player.sendMessage(chatColor("&6Вы не состоите в клане, в котором Вы можете быть заместителем."));
                    return;
                }

                if (args.length != 3) {
                    player.sendMessage(chatColor("&7/clan edit-role <id> <название> - Редактировать роль."));
                    player.sendMessage(chatColor("&fID роли можно посмотреть в списке ролей Вашего клана."));
                    return;
                }

                int id = Integer.parseInt(args[1]);
                String name = args[2];
                if (!(id >= 0 && id < clan.getRoles().size())) {
                    player.sendMessage(chatColor("&cРоль не найдена."));
                    player.sendMessage(chatColor("&fID роли можно посмотреть в списке ролей Вашего клана."));
                    return;
                }

                Role role = clan.getRoles().get(id);
                role.setName(name);
                plugin.getMySQL().editRole(clan, role, "UPDATE roles SET name = ? WHERE id = ? AND clan_id = ?");

                String line = chatColor(plugin.getDefaultConfig().getString("messages.edit-role")
                        .replace("%string_value%", name)
                );
                player.sendMessage(line);
                return;
            }

            case "home-create": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerDeputyClan(uuid);
                if (clan == null) {
                    player.sendMessage(chatColor("&6Вы не состоите в клане, в котором Вы можете быть заместителем."));
                    return;
                }

                if (args.length != 2) {
                    player.sendMessage(chatColor("&7/clan home-create <название> - Создать точку дома."));
                    return;
                }

                String homeName = args[1];
                Location location = player.getLocation();
                Home home = new Home(
                        0,
                        clan.getId(), homeName,
                        location.getWorld(),
                        location.getX(), location.getY(), location.getZ()
                );

                boolean isSuccess = plugin.getMySQL().addClanHome(
                        clan,
                        home,
                        "INSERT INTO homes (clan_id, name, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?)"
                );

                if (!isSuccess) {
                    player.sendMessage(chatColor("&cКлан хом не создался. Сообщите разработчику!"));
                    return;
                }

                String line = chatColor(plugin.getDefaultConfig().getString("messages.add-clan-home")
                        .replace("%string_value%", homeName)
                );
                player.sendMessage(line);
                return;
            }

            case "home-remove": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerDeputyClan(uuid);
                if (clan == null) {
                    player.sendMessage(chatColor("&6Вы не состоите в клане, в котором Вы можете быть заместителем."));
                    return;
                }

                if (args.length != 2) {
                    player.sendMessage(chatColor("&7/clan home-remove <id> - Удалить точку дома."));
                    player.sendMessage(chatColor("&fID точки дома можно посмотреть в списке домов Вашего клана"));
                    return;
                }

                int id = Integer.parseInt(args[1]);
                if (!(id >= 0 && id < clan.getHomes().size())) {
                    player.sendMessage(chatColor("&cТочка дома не найдена."));
                    player.sendMessage(chatColor("&fID точки дома можно посмотреть в списке домов Вашего клана"));
                    return;
                }

                Home home = clan.getHomes().get(id);
                String homeName = home.getName();

                boolean isSuccess = plugin.getMySQL().removeClanHome(
                        clan, home, "DELETE FROM homes WHERE id = ? AND clan_id = ?"
                );

                if (!isSuccess) {
                    player.sendMessage(chatColor("&cКлановый дом не удалился. Сообщите разработчику!"));
                    return;
                }

                String line = chatColor(plugin.getDefaultConfig().getString("messages.remove-clan-home")
                        .replace("%string_value%", homeName)
                );
                player.sendMessage(line);
                return;
            }

            case "delete": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerDeputyClan(uuid);
                if (clan == null) {
                    player.sendMessage(chatColor("&6Вы не состоите в клане, в котором Вы можете быть заместителем."));
                    return;
                }

                boolean isSuccess = plugin.getMySQL().deleteClan(clan, "DELETE FROM clans WHERE id = ?");
                if (!isSuccess) {
                    player.sendMessage(chatColor("&cНе удалось удалить клан. Сообщите разработчику!"));
                    return;
                }

                player.sendMessage(chatColor("&6Клан был успешно удалён."));
                return;
            }

            case "set-member-role": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerDeputyClan(uuid);
                if (clan == null) {
                    player.sendMessage(chatColor("&6Вы не состоите в клане, в котором Вы можете быть заместителем."));
                    return;
                }

                if (args.length != 3) {
                    player.sendMessage(chatColor(
                            "&7/clan set-member-role <игрок> <id> - Установить участнику роль.")
                    );
                    player.sendMessage(chatColor("&fID роли можно посмотреть в списке ролей Вашего клана."));
                    return;
                }

                String playerName = args[1];
                int id = Integer.parseInt(args[2]);
                if (!(id >= 0 && id < clan.getRoles().size())) {
                    player.sendMessage(chatColor("&cРоль не найдена."));
                    player.sendMessage(chatColor("&fID роли можно посмотреть в списке ролей Вашего клана."));
                    return;
                }

                Role role = clan.getRoles().get(id);
                Player target = Bukkit.getPlayer(playerName);
                if (target == null) return;
                UUID uuidTarget = target.getUniqueId();
                Member member = plugin.getClanUtil().getMemberByUUID(uuidTarget, clan);
                if (member == null) {
                    player.sendMessage(chatColor("&6Этот игрок не состоит в Вашем клане."));
                    return;
                }

                member.setRole(id);
                plugin.getMySQL().editMember(
                        clan, member, "UPDATE clan_members SET role = ? WHERE id = ? AND clan_id = ?"
                );

                String line = chatColor(plugin.getDefaultConfig().getString("messages.set-member-role")
                        .replace("%string_value%", role.getName())
                );
                player.sendMessage(line);
                return;
            }

            case "leave": {
                UUID uuid = player.getUniqueId();
                Clan clan = plugin.getClanUtil().searchPlayerClan(uuid);
                if (clan == null) return;
                if (clan.getOwner() == uuid) {
                    player.sendMessage(chatColor("&7Вы не можете покинуть свой клан. /clan delete."));
                    return;
                }

                Member member = plugin.getClanUtil().getMemberByUUID(uuid, clan);
                if (member == null) return;

                List<String> leaveMessage = plugin.getDefaultConfig().getStringList("leave-message");
                for (String s : leaveMessage) {
                    plugin.getPlayersLeaves().add(uuid);
                    TextComponent message = new TextComponent(chatColor(s));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept_clan_leave"));
                    player.sendMessage(message);
                }
            }
        }
    }

    @Override
    public List<String> completer(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
