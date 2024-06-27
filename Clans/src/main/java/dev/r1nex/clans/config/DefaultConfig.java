package dev.r1nex.clans.config;

import dev.r1nex.clans.Clans;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class DefaultConfig {
    private final FileConfiguration fc;
    private final HashMap<String, String> strings = new HashMap<>(6);
    private final HashMap<String, Integer> integers = new HashMap<>(5);
    private final HashMap<String, List<String>> listStrings = new HashMap<>(2);

    public DefaultConfig(Clans plugin) {
        if (!new File(plugin.getDataFolder() + File.separator + "config.yml").exists()) {
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveDefaultConfig();
        }
        this.fc = plugin.getConfig();
        loadLines();
    }

    public void loadLines() {
        // -- Loaded strings
        strings.put("messages.create-clan", fc.getString("messages.create-clan"));
        strings.put("messages.delete-clan", fc.getString("messages.delete-clan"));
        strings.put("messages.add-member", fc.getString("messages.add-member"));
        strings.put("messages.remove-member", fc.getString("messages.remove-member"));
        strings.put("messages.clan-pvp-off", fc.getString("messages.clan-pvp-off"));
        strings.put("messages.role-add", fc.getString("messages.role-add"));
        strings.put("messages.role-remove", fc.getString("messages.role-remove"));
        strings.put("messages.edit-role", fc.getString("messages.edit-role"));
        strings.put("messages.set-member-role", fc.getString("messages.set-member-role"));
        strings.put("messages.add-clan-home", fc.getString("messages.add-clan-home"));
        strings.put("messages.remove-clan-home", fc.getString("messages.remove-clan-home"));
        strings.put("messages.add-clan-desc", fc.getString("messages.add-clan-desc"));
        strings.put("messages.remove-clan-desc", fc.getString("messages.remove-clan-desc"));
        strings.put("messages.clan-leave", fc.getString("messages.clan-leave"));
        strings.put("clan.chat", fc.getString("clan.chat"));
        strings.put("clan.symbol", fc.getString("clan.symbol"));

        // -- Loaded integers
        integers.put("settings-server.max-members-clan", fc.getInt("settings-server.max-members-clan"));
        integers.put("settings-server.min-clan-name", fc.getInt("settings-server.min-clan-name"));
        integers.put("settings-server.max-clan-name", fc.getInt("settings-server.max-clan-name"));
        integers.put("settings-server.max-clan-roles", fc.getInt("settings-server.max-clan-roles"));
        integers.put("settings-server.mob-kill-rating", fc.getInt("settings-server.mob-kill-rating"));

        // -- Loaded lists
        listStrings.put("clan-description-in-gui", fc.getStringList("clan-description-in-gui"));
        listStrings.put("commands-format", fc.getStringList("commands-format"));
        listStrings.put("kill-mobs", fc.getStringList("kill-mobs"));
        listStrings.put("invite-message", fc.getStringList("invite-message"));
        listStrings.put("leave-message", fc.getStringList("leave-message"));
    }

    public String getString(String path) {
        return strings.get(path);
    }

    public int getInt(String path) {
        return integers.get(path);
    }

    public List<String> getStringList(String path) {
        return listStrings.get(path);
    }
}
