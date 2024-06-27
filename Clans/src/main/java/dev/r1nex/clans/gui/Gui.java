package dev.r1nex.clans.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.r1nex.clans.Clans;
import dev.r1nex.clans.data.Clan;
import dev.r1nex.clans.data.Home;
import dev.r1nex.clans.data.Member;
import dev.r1nex.clans.data.Role;
import dev.r1nex.clans.utils.ItemCreate;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Gui {
    private final Clans plugin;
    private final String title;
    private final int size;

    public Gui(Clans plugin, int size, String title) {
        this.plugin = plugin;
        this.size = size;
        this.title = title;
    }

    private String chatColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void openAllClans(Player player) {
        ChestGui chestGui = new ChestGui(size, chatColor(title));
        chestGui.setOnGlobalClick(event -> event.setCancelled(true));
        PaginatedPane pane = new PaginatedPane(1, 1, 7, 4);
        List<GuiItem> items = new ArrayList<>();
        plugin.getClans().forEach(clan -> {
            List<String> lore = plugin.getDefaultConfig().getStringList("clan-description-in-gui");
            ItemStack itemStack = new ItemStack(Material.ITEM_FRAME);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.lore(plugin.getItemUtil().lore(lore, clan));
            itemStack.setItemMeta(itemMeta);

            GuiItem guiItem = new GuiItem(itemStack, event -> {
                openAllMembersClan(player, clan);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1);
            });
            items.add(guiItem);
        });
        pane.populateWithGuiItems(items);

        ItemCreate nextPage = new ItemCreate(Material.PAPER, 1, 10135, "&aСлед. страница");
        ItemCreate backPage = new ItemCreate(Material.PAPER, 1, 10137, "&cПред. страница");
        ItemCreate itemCreateClan = new ItemCreate(
                Material.TOTEM_OF_UNDYING, 1, -1, "&6Создать клан."
        );

        StaticPane navigation = new StaticPane(0, 1, 9, 5);
        navigation.addItem(new GuiItem(backPage.getItemStack(), event -> {
            if (pane.getPage() > 0) {
                pane.setPage(pane.getPage() - 1);

                chestGui.update();
            }
        }), 2, 4);
        navigation.addItem(new GuiItem(nextPage.getItemStack(), event -> {
            if (pane.getPage() < pane.getPages() - 1) {
                pane.setPage(pane.getPage() + 1);

                chestGui.update();
            }
        }), 6, 4);
        navigation.addItem(new GuiItem(itemCreateClan.getItemStack(), event -> {
            player.sendMessage(chatColor("&8| &6/clan create (название) - &fСоздать клан."));
            player.closeInventory();
        }), 4, 4);

        chestGui.addPane(navigation);
        chestGui.addPane(pane);
        chestGui.show(player);
    }

    public void openAllMembersClan(Player player, Clan clan) {
        ChestGui chestGui = new ChestGui(size, chatColor(title));
        chestGui.setOnGlobalClick(event -> event.setCancelled(true));
        PaginatedPane pane = new PaginatedPane(1, 1, 7, 4);
        List<GuiItem> items = new ArrayList<>();
        for (Member member : clan.getMembers()) {
            String roleName = "&cUNKNOWN";
            Role role = plugin.getClanUtil().getRoleByMember(member.getMember());
            if (role != null) roleName = role.getName();
            Player p = Bukkit.getPlayer(member.getMember());
            if (p == null) continue;
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Component.text(chatColor("&7" + p.getName() + " " + "&7(" + roleName + "&7)")));
            itemStack.setItemMeta(itemMeta);
            GuiItem guiItem = new GuiItem(itemStack, event ->
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1)
            );
            items.add(guiItem);
        }
        pane.populateWithGuiItems(items);

        ItemCreate nextPage = new ItemCreate(Material.PAPER, 1, 10135, "&aСлед. страница");
        ItemCreate backPage = new ItemCreate(Material.PAPER, 1, 10137, "&cПред. страница");
        ItemCreate options = new ItemCreate(Material.COMPARATOR, 1, 0, "&6Настройки клана.");
        ItemCreate backButton = new ItemCreate(
                Material.PAPER, 1, 10419, chatColor("&6Вернуться в список кланов.")
        );

        StaticPane navigation = new StaticPane(0, 1, 9, 5);
        navigation.addItem(new GuiItem(backPage.getItemStack(), event -> {
            if (pane.getPage() > 0) {
                pane.setPage(pane.getPage() - 1);

                chestGui.update();
            }
        }), 2, 4);

        navigation.addItem(new GuiItem(nextPage.getItemStack(), event -> {
            if (pane.getPage() < pane.getPages() - 1) {
                pane.setPage(pane.getPage() + 1);

                chestGui.update();
            }
        }), 6, 4);

        navigation.addItem(new GuiItem(options.getItemStack(), event -> openOptionsClan(player, clan)), 1, 4);
        navigation.addItem(new GuiItem(backButton.getItemStack(), event -> openAllClans(player)), 4, 4);

        chestGui.addPane(navigation);
        chestGui.addPane(pane);
        chestGui.show(player);
    }

    public void openOptionsClan(Player player, Clan clan) {
        ChestGui chestGui = new ChestGui(size, chatColor(title));
        chestGui.setOnGlobalClick(event -> event.setCancelled(true));

        StaticPane navigation = new StaticPane(0, 1, 9, 5);

        ItemCreate rolesButton = new ItemCreate(
                Material.ANVIL, 1, -1, chatColor("&6Настройки ролей")
        );
        ItemCreate backButton = new ItemCreate(
                Material.PAPER, 1, 10419, chatColor("&6Вернуться в список участников.")
        );
        ItemCreate buttonHomes = new ItemCreate(
                Material.ACACIA_DOOR, 1, -1, chatColor("&7Точки домов.")
        );
        ItemCreate buttonClanPvP = new ItemCreate(
                Material.DIAMOND_AXE, 1, -1, chatColor("&7Клановое &cPvP.")
        );

        navigation.addItem(new GuiItem(rolesButton.getItemStack(), event ->
                openAllRolesClan(player, clan)), 1, 0
        );

        navigation.addItem(new GuiItem(buttonHomes.getItemStack(), event -> {
            if (plugin.getClanUtil().getMemberByUUID(player.getUniqueId(), clan) == null) return;
            openAllHomesClan(player, clan);
        }), 3, 0);

        navigation.addItem(new GuiItem(buttonClanPvP.getItemStack(), event -> {
            if (!plugin.getClanUtil().isPlayerDeputyClan(player.getUniqueId(), clan)) return;

            if (clan.isPvp()) {
                player.sendMessage(chatColor("&6Клановое PvP &cвыключено."));
                clan.setPvp(false);
            } else {
                player.sendMessage(chatColor("&6Клановое PvP &aвключено."));
                clan.setPvp(true);
            }
        }), 5, 0);

        navigation.addItem(new GuiItem(backButton.getItemStack(), event ->
                openAllMembersClan(player, clan)), 4, 4
        );

        chestGui.addPane(navigation);
        chestGui.show(player);
    }

    public void openAllRolesClan(Player player, Clan clan) {
        ChestGui chestGui = new ChestGui(size, chatColor(title));
        chestGui.setOnGlobalClick(event -> event.setCancelled(true));
        PaginatedPane pane = new PaginatedPane(1, 1, 7, 4);
        List<GuiItem> items = new ArrayList<>();
        for (Role role : clan.getRoles()) {
            int index = clan.getRoles().indexOf(role);
            ItemStack itemStack = new ItemStack(Material.NAME_TAG);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Component.text(chatColor(role.getName() + " " + "&f(&7" + index + "&f)")));
            itemStack.setItemMeta(itemMeta);
            GuiItem guiItem = new GuiItem(itemStack, event ->
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1)
            );
            items.add(guiItem);
        }
        pane.populateWithGuiItems(items);

        ItemCreate nextPage = new ItemCreate(Material.PAPER, 1, 10135, "&aСлед. страница");
        ItemCreate backPage = new ItemCreate(Material.PAPER, 1, 10137, "&cПред. страница");
        ItemCreate backButton = new ItemCreate(
                Material.PAPER, 1, 10419, chatColor("&6Вернуться в опции.")
        );

        StaticPane navigation = new StaticPane(0, 1, 9, 5);
        navigation.addItem(new GuiItem(backPage.getItemStack(), event -> {
            if (pane.getPage() > 0) {
                pane.setPage(pane.getPage() - 1);

                chestGui.update();
            }
        }), 2, 4);

        navigation.addItem(new GuiItem(nextPage.getItemStack(), event -> {
            if (pane.getPage() < pane.getPages() - 1) {
                pane.setPage(pane.getPage() + 1);

                chestGui.update();
            }
        }), 6, 4);

        navigation.addItem(new GuiItem(backButton.getItemStack(), event -> openOptionsClan(player, clan)), 4, 4);

        chestGui.addPane(navigation);
        chestGui.addPane(pane);
        chestGui.show(player);
    }

    public void openAllHomesClan(Player player, Clan clan) {
        ChestGui chestGui = new ChestGui(size, chatColor(title));
        chestGui.setOnGlobalClick(event -> event.setCancelled(true));
        PaginatedPane pane = new PaginatedPane(1, 1, 7, 4);
        List<GuiItem> items = new ArrayList<>();
        for (Home home : clan.getHomes()) {
            int index = clan.getHomes().indexOf(home);
            ItemStack itemStack = new ItemStack(Material.BOOK);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Component.text(chatColor(home.getName()  + " " + "&f(&7" + index + "&f)")));
            itemStack.setItemMeta(itemMeta);
            GuiItem guiItem = new GuiItem(itemStack, event -> {
                Location location = new Location(Bukkit.getWorld("world"), home.getX(), home.getY(), home.getZ());
                player.teleport(location);
                player.closeInventory();
            });
            items.add(guiItem);
        }

        ItemCreate backButton = new ItemCreate(
                Material.PAPER, 1, 10419, chatColor("&6Вернуться в опции.")
        );
        ItemCreate nextPage = new ItemCreate(Material.PAPER, 1, 10135, "&aСлед. страница");
        ItemCreate backPage = new ItemCreate(Material.PAPER, 1, 10137, "&cПред. страница");

        StaticPane navigation = new StaticPane(0, 1, 9, 5);
        navigation.addItem(new GuiItem(backPage.getItemStack(), event -> {
            if (pane.getPage() > 0) {
                pane.setPage(pane.getPage() - 1);

                chestGui.update();
            }
        }), 2, 4);

        navigation.addItem(new GuiItem(nextPage.getItemStack(), event -> {
            if (pane.getPage() < pane.getPages() - 1) {
                pane.setPage(pane.getPage() + 1);

                chestGui.update();
            }
        }), 6, 4);

        navigation.addItem(new GuiItem(backButton.getItemStack(), event -> openOptionsClan(player, clan)), 4, 4);

        pane.populateWithGuiItems(items);
        chestGui.addPane(navigation);
        chestGui.addPane(pane);
        chestGui.show(player);
    }

    public void acceptClanCreate(Player player, Clan clan) {
        ChestGui chestGui = new ChestGui(size, chatColor(title));
        chestGui.setOnGlobalClick(event -> event.setCancelled(true));
        ItemCreate buttonYes = new ItemCreate(Material.PAPER, 1, 10415, "&aДА (100.000$)");
        ItemCreate buttonNo = new ItemCreate(Material.PAPER, 1, 10419, "&cНЕТ");

        StaticPane buttons = new StaticPane(0, 2, 9, 1);
        buttons.addItem(new GuiItem(buttonYes.getItemStack(), event -> {
            if (!plugin.getEconomyUtils().takeMoney(player, 100000)) {
                player.sendMessage(chatColor("&6Вы не смогли создать клан. Нужно иметь на счету &a100.000$"));
                return;
            }

            boolean isSuccess = plugin.getMySQL().addClan(
                    clan,
                    "INSERT INTO clans (owner, name, currency, rating) VALUES (?, ?, ?, ?)"
            );

            if (!isSuccess) {
                player.sendMessage(chatColor("&cОшибка создания клана "));
                return;
            }
            String line = chatColor(plugin.getDefaultConfig().getString("messages.create-clan")
                    .replace("%string_value%", clan.getName())
            );
            player.sendMessage(line);
            player.sendMessage(chatColor("&6С Вашего счёта снято 100.000$"));
            player.closeInventory();
        }), 3, 0);

        buttons.addItem(new GuiItem(buttonNo.getItemStack(), event -> {
            player.sendMessage(chatColor("&6Вы отказались от создания клана."));
            player.closeInventory();
        }), 5, 0);

        chestGui.addPane(buttons);
        chestGui.show(player);
    }
}
