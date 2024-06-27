package dev.r1nex.clans.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.r1nex.clans.Clans;
import dev.r1nex.clans.data.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MySQL {

    private final Clans plugin;

    private final HikariDataSource source;

    public MySQL(Clans plugin, String host, int port, String database, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("maximumPoolSize", 30);
        this.source = new HikariDataSource(config);
        this.plugin = plugin;
    }

    private Connection connection() throws SQLException {
        return source.getConnection();
    }

    private void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (MySQL.this) {
                runnable.run();
            }
        });
    }

    public void createTables() {
        try (Connection connection = connection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `clans` (" +
                        "`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "`owner` VARCHAR(36) NOT NULL, " +
                        "`name` VARCHAR(128) NOT NULL, " +
                        "`currency` INTEGER NOT NULL, " +
                        "`rating` INTEGER NOT NULL)"
                );

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `clan_members` (" +
                        "`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "`clan_id` INTEGER NOT NULL, " +
                        "`uuid` VARCHAR(36) NOT NULL, " +
                        "`role` INTEGER NOT NULL)"
                );

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `homes` (" +
                        "`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "`clan_id` INTEGER NOT NULL, " +
                        "`name` VARCHAR(90) NOT NULL, " +
                        "`world` VARCHAR(56) NOT NULL, " +
                        "`x` DOUBLE NOT NULL, " +
                        "`y` DOUBLE NOT NULL, " +
                        "`z` DOUBLE NOT NULL)"
                );

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `roles` (" +
                        "`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "`clan_id` INTEGER NOT NULL, " +
                        "`name` VARCHAR(96) NOT NULL)"
                );

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `clan_desc` (" +
                        "`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "`clan_id` INTEGER NOT NULL, " +
                        "`text` VARCHAR(64) NOT NULL)"
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Clan> getAllClans() {
        CompletableFuture<List<Clan>> future = CompletableFuture.supplyAsync(() -> {
            List<Clan> clans = new ArrayList<>();

            try (Connection connection = connection()) {
                String sql =
                        "SELECT clans.id, clans.owner, clans.currency, clans.rating, clans.name, " +
                                "(SELECT GROUP_CONCAT(clan_members.id) FROM clan_members WHERE clans.id = clan_members.clan_id) AS ids_list_roles, " +
                                "(SELECT GROUP_CONCAT(clan_members.uuid) FROM clan_members WHERE clans.id = clan_members.clan_id) AS uuid_list, " +
                                "(SELECT GROUP_CONCAT(clan_members.role) FROM clan_members WHERE clans.id = clan_members.clan_id) AS role_list, " +
                                "(SELECT GROUP_CONCAT(roles.id) FROM roles WHERE clans.id = roles.clan_id) AS role_id_list, " +
                                "(SELECT GROUP_CONCAT(roles.name) FROM roles WHERE clans.id = roles.clan_id) AS role_name_list, " +
                                "(SELECT GROUP_CONCAT(CONCAT(homes.x, ',', homes.y, ',', homes.z, ',', homes.world, ',' , homes.id, ',', homes.name)) FROM homes WHERE clans.id = homes.clan_id) AS homes_data " +
                                "FROM clans";

                try (PreparedStatement ps = connection.prepareStatement(sql);
                     ResultSet resultSet = ps.executeQuery()) {

                    while (resultSet.next()) {
                        int clanId = resultSet.getInt("id");
                        UUID owner = UUID.fromString(resultSet.getString("owner"));
                        int currency = resultSet.getInt("currency");
                        int rating = resultSet.getInt("rating");
                        String name = resultSet.getString("name");
                        String idsListMembersRoles = resultSet.getString("ids_list_roles");
                        String uuidList = resultSet.getString("uuid_list");
                        String roleList = resultSet.getString("role_list");
                        String roleIdList = resultSet.getString("role_id_list");
                        String roleNameList = resultSet.getString("role_name_list");
                        String homeData = resultSet.getString("homes_data");

                        List<Member> members = getMembers(uuidList, roleList, idsListMembersRoles, clanId);
                        List<Role> roles = getRoles(roleIdList, roleNameList, clanId);
                        List<Home> homes = getHomes(homeData, clanId);
                        Clan clan = new Clan(
                                clanId, owner, name,
                                currency, rating, members,
                                roles, homes
                        );

                        clans.add(clan);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return clans;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static List<Member> getMembers(String uuidList, String roleList, String idsRolesList, int clanId) {
        List<Member> members = new ArrayList<>();
        if (uuidList != null && roleList != null && idsRolesList != null) {
            String[] ids = idsRolesList.split(",");
            String[] uuids = uuidList.split(",");
            String[] roles = roleList.split(",");
            for (int i = 0; i < ids.length; i++) {
                int id = Integer.parseInt(ids[i]);
                UUID uuid = UUID.fromString(uuids[i]);
                int role = Integer.parseInt(roles[i]);
                Member member = new Member(id, clanId, uuid, role);
                members.add(member);
            }
        }
        return members;
    }

    @NotNull
    private static List<Home> getHomes(String homeData, int clanId) {
        List<Home> homes = new ArrayList<>();
        if (homeData != null) {
            String[] homeInfo = homeData.split(",");
            for (int i = 0; i < homeInfo.length; i += 6) {
                double x = Double.parseDouble(homeInfo[i]);
                double y = Double.parseDouble(homeInfo[i + 1]);
                double z = Double.parseDouble(homeInfo[i + 2]);
                String stringWorld = homeInfo[i + 3];
                int homeId = Integer.parseInt(homeInfo[i + 4]);
                String homeName = homeInfo[i + 5];
                World world = Bukkit.getWorld(stringWorld);
                Home home = new Home(homeId, clanId, homeName, world, x, y, z);
                homes.add(home);
            }
        }
        return homes;
    }

    @NotNull
    private static List<Role> getRoles(String roleIdList, String roleNameList, int clanId) {
        List<Role> roles = new ArrayList<>();
        if (roleIdList != null && roleNameList != null) {
            String[] roleIds = roleIdList.split(",");
            String[] roleNames = roleNameList.split(",");
            for (int i = 0; i < roleIds.length; i++) {
                int roleId = Integer.parseInt(roleIds[i]);
                String roleName = roleNames[i];
                Role role = new Role(clanId, roleId, roleName);
                roles.add(role);
            }
        }
        return roles;
    }


    public boolean addClan(Clan clan, String query) {
        try (Connection connection = connection()) {
            try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, clan.getOwner().toString());
                ps.setString(2, clan.getName());
                ps.setInt(3, 0);
                ps.setInt(4, 0);
                ps.executeUpdate();

                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt(1);
                        clan.setId(id);
                        Member member = new Member(0, id, clan.getOwner(), 11);
                        boolean isSuccess = addMember(
                                clan, member,
                                "INSERT INTO clan_members (clan_id, uuid, role) VALUES (?, ?, ?)"
                        );

                        if (!isSuccess) {
                            return false;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            return false;
        }
        plugin.getClans().add(clan);
        return plugin.getClans().contains(clan);
    }

    public boolean addMember(Clan clan, Member member, String query) {
        AtomicBoolean success = new AtomicBoolean(true);
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, clan.getId());
                    ps.setString(2, member.getMember().toString());
                    ps.setInt(3, member.getRole());
                    ps.executeUpdate();
                    member.setClanId(clan.getId());

                    try (ResultSet resultSet = ps.getGeneratedKeys()) {
                        while (resultSet.next()) {
                            int id = resultSet.getInt(1);
                            member.setId(id);
                        }
                    }

                    clan.getMembers().add(member);
                }
            } catch (SQLException e) {
                success.set(false);
            }
        });
        return success.get();
    }

    public void editMember(Clan clan, Member member, String query) {
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, member.getRole());
                    ps.setInt(2, member.getId());
                    ps.setInt(3, clan.getId());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean removeMember(Clan clan, Member member, String query) {
        AtomicBoolean success = new AtomicBoolean(true);
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, clan.getId());
                    ps.setString(2, member.getMember().toString());
                    ps.executeUpdate();
                    clan.getMembers().remove(member);
                }
            } catch (SQLException e) {
                success.set(false);
            }
        });
        return success.get();
    }

    public boolean addRole(Clan clan, Role role, String query) {
        AtomicBoolean success = new AtomicBoolean(true);
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, clan.getId());
                    ps.setString(2, role.getName());
                    ps.executeUpdate();

                    try (ResultSet resultSet = ps.getGeneratedKeys()) {
                        while (resultSet.next()) {
                            int id = resultSet.getInt(1);
                            role.setId(id);
                        }
                    }

                    clan.getRoles().add(role);
                }
            } catch (SQLException e) {
                success.set(false);
            }
        });
        return success.get();
    }

    public void editRole(Clan clan, Role role, String query) {
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, role.getName());
                    ps.setInt(2, role.getId());
                    ps.setInt(3, clan.getId());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean removeRole(Clan clan, Role role, String query) {
        AtomicBoolean success = new AtomicBoolean(true);
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, role.getId());
                    ps.setInt(2, clan.getId());
                    ps.executeUpdate();
                    clan.getRoles().remove(role);
                }
            } catch (SQLException e) {
                success.set(false);
            }
        });
        return success.get();
    }

    public boolean addClanHome(Clan clan, Home home, String query) {
        AtomicBoolean success = new AtomicBoolean(true);
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, clan.getId());
                    ps.setString(2, home.getName());
                    ps.setString(3, home.getWorld().getName());
                    ps.setDouble(4, home.getX());
                    ps.setDouble(5, home.getY());
                    ps.setDouble(6, home.getZ());
                    ps.executeUpdate();
                    try (ResultSet resultSet = ps.getGeneratedKeys()) {
                        while (resultSet.next()) {
                            int id = resultSet.getInt(1);
                            home.setId(id);
                        }
                    }

                    clan.getHomes().add(home);
                }
            } catch (SQLException e) {
                success.set(false);
            }
        });
        return success.get();
    }

    public boolean removeClanHome(Clan clan, Home home, String query) {
        AtomicBoolean success = new AtomicBoolean(true);
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, home.getId());
                    ps.setInt(2, clan.getId());
                    ps.executeUpdate();
                    clan.getHomes().remove(home);
                }
            } catch (SQLException e) {
                success.set(false);
            }
        });
        return success.get();
    }

    public boolean deleteClan(Clan clan, String query) {
        AtomicBoolean success = new AtomicBoolean(true);
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, clan.getId());
                    ps.executeUpdate();
                    for (Member member : clan.getMembers()) {
                        boolean isSuccess = removeMember(
                                clan, member, "DELETE FROM clan_members WHERE clan_id = ? AND uuid = ?"
                        );

                        if (!isSuccess) {
                            success.set(false);
                            return;
                        }
                    }

                    for (Role role : clan.getRoles()) {
                        boolean isSuccess = removeRole(
                                clan, role, "DELETE FROM roles WHERE id = ? AND clan_id = ?"
                        );

                        if (!isSuccess) {
                            success.set(false);
                            return;
                        }
                    }

                    for (Home home : clan.getHomes()) {
                        boolean isSuccess = removeClanHome(
                                clan, home, "DELETE FROM homes WHERE id = ? AND clan_id = ?"
                        );

                        if (!isSuccess) {
                            success.set(false);
                            return;
                        }
                    }

                    plugin.getClans().remove(clan);
                }
            } catch (SQLException e) {
                success.set(false);
            }
        });
        return success.get();
    }

    public void updateClan(Clan clan, String query) {
        async(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, clan.getCurrency());
                    ps.setInt(2, clan.getRating());
                    ps.setInt(3, clan.getId());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
