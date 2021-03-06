package fi.matiaspaavilainen.masuitecore.managers;

import fi.matiaspaavilainen.masuitecore.Debugger;
import fi.matiaspaavilainen.masuitecore.MaSuiteCore;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitecore.database.Database;
import fi.matiaspaavilainen.masuitecore.listeners.MaSuitePlayerGroup;
import fi.matiaspaavilainen.masuitecore.listeners.MaSuitePlayerLocation;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MaSuitePlayer {

    private Connection connection = null;
    private PreparedStatement statement = null;
    private Database db = MaSuiteCore.db;
    private Configuration config = new Configuration();
    private String tablePrefix = config.load(null, "config.yml").getString("database.table-prefix");
    private String username;
    private String nickname;
    private java.util.UUID UUID;
    private String ipAddress;
    private Long firstLogin;
    private Long lastLogin;
    private Location location;

    private Debugger debugger = new Debugger();

    public MaSuitePlayer() {
    }

    public MaSuitePlayer(String username, String nickname, java.util.UUID UUID, String ipAddress, Long firstLogin, Long lastLogin) {
        this.username = username;
        this.nickname = nickname;
        this.UUID = UUID;
        this.ipAddress = ipAddress;
        this.firstLogin = firstLogin;
        this.lastLogin = lastLogin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public java.util.UUID getUUID() {
        return UUID;
    }

    public void setUUID(java.util.UUID UUID) {
        this.UUID = UUID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Long getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(Long firstLogin) {
        this.firstLogin = firstLogin;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Deprecated
    public Location getLocation() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(this.UUID);
        if (locationGetter(b, out, p)) return new Location();
        final Location[] location = {MaSuitePlayerLocation.locations.get(p.getUniqueId())};
        ProxyServer.getInstance().getScheduler().schedule(new MaSuiteCore(), () -> {
            location[0] = MaSuitePlayerLocation.locations.get(this.UUID);
            debugger.sendMessage("[MaSuiteCore] [MaSuitePlayer] [Location] [MSP] returned location from plugin message.");
            MaSuitePlayerLocation.locations.remove(p.getUniqueId());
        }, 50, TimeUnit.MILLISECONDS);
        return location[0];
    }

    private boolean locationGetter(ByteArrayOutputStream b, DataOutputStream out, ProxiedPlayer p) {
        try {
            if (p == null) {
                return true;
            }
            out.writeUTF("MaSuitePlayerLocation");
            out.writeUTF(String.valueOf(this.UUID));
            p.getServer().sendData("BungeeCord", b.toByteArray());

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Deprecated
    public Location getLocation(UUID uuid) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(this.UUID);
        if (locationGetter(b, out, p)) return new Location();
        final Location[] location = {MaSuitePlayerLocation.locations.get(p.getUniqueId())};
        ProxyServer.getInstance().getScheduler().schedule(new MaSuiteCore(), () -> {
            location[0] = MaSuitePlayerLocation.locations.get(this.UUID);
            debugger.sendMessage("[MaSuiteCore] [MaSuitePlayer] [Location] [UUID] returned location from plugin message.");
            MaSuitePlayerLocation.locations.remove(p.getUniqueId());
        }, 50, TimeUnit.MILLISECONDS);
        return location[0];
    }

    @Deprecated
    public synchronized void requestLocation() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(this.UUID);
        try {
            if (p == null) {
                return;
            }
            out.writeUTF("MaSuitePlayerLocation");
            out.writeUTF(String.valueOf(this.UUID));
            p.getServer().sendData("BungeeCord", b.toByteArray());

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized Group getGroup() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(this.UUID);
        if (p == null) {
            return new Group();
        }
        // If Cache contains player
        if (MaSuitePlayerGroup.groups.containsKey(p.getUniqueId())) {
            debugger.sendMessage("[MaSuiteCore] [MaSuitePlayer] [Group] [MSP] returned group from cache.");
            return MaSuitePlayerGroup.groups.get(this.UUID);
        } else {
            try {
                out.writeUTF("MaSuitePlayerGroup");
                out.writeUTF(String.valueOf(this.UUID));
                p.getServer().sendData("BungeeCord", b.toByteArray());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            // Return group
            final Group[] group = {new Group()};
            ProxyServer.getInstance().getScheduler().schedule(new MaSuiteCore(), () -> {
                group[0] = MaSuitePlayerGroup.groups.get(this.UUID);
                debugger.sendMessage("[MaSuiteCore] [MaSuitePlayer] [Group] [MSP] returned group from plugin message.");
            }, 50, TimeUnit.MILLISECONDS);
            return group[0];
        }
    }

    public synchronized Group getGroup(UUID uuid) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(uuid);
        if (p == null) {
            return new Group();
        }
        // If Cache contains player
        if (MaSuitePlayerGroup.groups.containsKey(uuid)) {
            debugger.sendMessage("[MaSuite] [Core] [MaSuitePlayer] [Group] [UUID] returned group from cache.");
            return MaSuitePlayerGroup.groups.get(uuid);
        } else {
            try {
                out.writeUTF("MaSuitePlayerGroup");
                out.writeUTF(String.valueOf(uuid));
                p.getServer().sendData("BungeeCord", b.toByteArray());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            final Group[] group = {new Group()};
            ProxyServer.getInstance().getScheduler().schedule(new MaSuiteCore(), () -> {
                group[0] = MaSuitePlayerGroup.groups.get(uuid);
                debugger.sendMessage("[MaSuite] [Core] [MaSuitePlayer] [Group] [UUID] returned group from plugin message.");
            }, 50, TimeUnit.MILLISECONDS);
            return group[0];
        }
    }

    public void insert() {
        String insert = "INSERT INTO " + tablePrefix + "players (username, nickname, uuid, ipAddress, firstLogin, lastLogin) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username = ?, ipAddress = ?;";
        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement(insert);
            statement.setString(1, this.username);
            statement.setString(2, this.nickname);
            statement.setString(3, this.UUID.toString());
            statement.setString(4, this.ipAddress);
            statement.setLong(5, this.firstLogin);
            statement.setLong(6, this.lastLogin);
            statement.setString(7, this.username);
            statement.setString(8, this.ipAddress);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public MaSuitePlayer find(UUID uuid) {
        if (uuid == null) {
            System.out.println("[MaSuite] [Core] There was an error while getting [MaSuitePlayer]");
            return null;
        }
        MaSuitePlayer msp = new MaSuitePlayer();
        ResultSet rs = null;
        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "players WHERE uuid = ?;");
            statement.setString(1, String.valueOf(uuid));
            rs = statement.executeQuery();

            while (rs.next()) {
                msp.setUsername(rs.getString("username"));
                msp.setNickname(rs.getString("nickname"));
                msp.setUUID(java.util.UUID.fromString(rs.getString("uuid")));
                msp.setIpAddress(rs.getString("ipAddress"));
                msp.setFirstLogin(rs.getLong("firstLogin"));
                msp.setLastLogin(rs.getLong("lastLogin"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return msp;
    }

    public MaSuitePlayer find(String name) {
        MaSuitePlayer msp = new MaSuitePlayer();
        ResultSet rs = null;
        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "players WHERE username = ?;");
            statement.setString(1, name);
            rs = statement.executeQuery();
            while (rs.next()) {
                msp.setUsername(rs.getString("username"));
                msp.setNickname(rs.getString("nickname"));
                msp.setUUID(java.util.UUID.fromString(rs.getString("uuid")));
                msp.setIpAddress(rs.getString("ipAddress"));
                msp.setFirstLogin(rs.getLong("firstLogin"));
                msp.setLastLogin(rs.getLong("lastLogin"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return msp;
    }

    public Set<MaSuitePlayer> findAll() {
        Set<MaSuitePlayer> maSuitePlayers = new HashSet<>();
        ResultSet rs = null;
        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "players;");
            rs = statement.executeQuery();
            while (rs.next()) {
                MaSuitePlayer msp = new MaSuitePlayer();
                msp.setUsername(rs.getString("username"));
                msp.setNickname(rs.getString("nickname"));
                msp.setUUID(java.util.UUID.fromString(rs.getString("uuid")));
                msp.setIpAddress(rs.getString("ipAddress"));
                msp.setFirstLogin(rs.getLong("firstLogin"));
                msp.setLastLogin(rs.getLong("lastLogin"));
                maSuitePlayers.add(msp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return maSuitePlayers;
    }


    public void update(MaSuitePlayer msp) {
        String update = "UPDATE " + tablePrefix + "players SET username = ?, nickname = ?, ipAddress = ?, lastLogin = ? WHERE uuid = ?";
        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement(update);
            statement.setString(1, msp.getUsername());
            statement.setString(2, msp.getNickname());
            statement.setString(3, msp.getIpAddress());
            statement.setLong(4, msp.getLastLogin());
            statement.setString(5, msp.getUUID().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
