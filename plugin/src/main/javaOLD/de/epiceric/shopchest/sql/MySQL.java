package de.epiceric.shopchest.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import de.epiceric.shopchest.config.GlobalConfig;
import org.bukkit.scheduler.BukkitRunnable;

import de.epiceric.shopchest.ShopChest;

public class MySQL extends Database {

    public MySQL(ShopChest plugin) {
        super(plugin);
    }

    @Override
    HikariDataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        // TODO Inspect this
        //  Why mariadb instead of my sql dani ?
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s",
                GlobalConfig.databaseMySqlHost, GlobalConfig.databaseMySqlPort, GlobalConfig.databaseMySqlDatabase));
        config.setUsername(GlobalConfig.databaseMySqlUsername);
        config.setPassword(GlobalConfig.databaseMySqlPassword);
        config.setConnectionTestQuery("SELECT 1");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return new HikariDataSource(config);
    }

    /**
     * Sends an asynchronous ping to the database
     */
    public void ping() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        Statement s = con.createStatement()) {
                    plugin.getDebugLogger().debug("Pinging to MySQL server...");
                    s.execute("/* ping */ SELECT 1");
                } catch (SQLException ex) {
                    plugin.getLogger().severe("Failed to ping to MySQL server. Trying to reconnect...");
                    plugin.getDebugLogger().debug("Failed to ping to MySQL server. Trying to reconnect...");
                    connect(null);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    String getQueryCreateTableShops() {
        return "CREATE TABLE IF NOT EXISTS " + tableShops + " ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "vendor TINYTEXT NOT NULL,"
            + "product TEXT NOT NULL,"
            + "amount INTEGER NOT NULL,"
            + "world TINYTEXT NOT NULL,"
            + "x INTEGER NOT NULL,"
            + "y INTEGER NOT NULL,"
            + "z INTEGER NOT NULL,"
            + "buyprice FLOAT NOT NULL,"
            + "sellprice FLOAT NOT NULL,"
            + "shoptype TINYTEXT NOT NULL)";
    }

    @Override
    String getQueryCreateTableLog() {
        return "CREATE TABLE IF NOT EXISTS " + tableLogs + " ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "shop_id INTEGER NOT NULL,"
            + "timestamp TINYTEXT NOT NULL,"
            + "time LONG NOT NULL,"
            + "player_name TINYTEXT NOT NULL,"
            + "player_uuid TINYTEXT NOT NULL,"
            + "product_name TINYTEXT NOT NULL,"
            + "product TEXT NOT NULL,"
            + "amount INTEGER NOT NULL,"
            + "vendor_name TINYTEXT NOT NULL,"
            + "vendor_uuid TINYTEXT NOT NULL,"
            + "admin BIT NOT NULL,"
            + "world TINYTEXT NOT NULL,"
            + "x INTEGER NOT NULL,"
            + "y INTEGER NOT NULL,"
            + "z INTEGER NOT NULL,"
            + "price FLOAT NOT NULL,"
            + "type TINYTEXT NOT NULL)";
    }

    @Override
    String getQueryCreateTableLogout() {
        return "CREATE TABLE IF NOT EXISTS " + tableLogouts + " ("
            + "player VARCHAR(36) PRIMARY KEY NOT NULL,"
            + "time LONG NOT NULL)";
    }

    @Override
    String getQueryCreateTableFields() {
        return "CREATE TABLE IF NOT EXISTS " + tableFields + " ("
            + "field VARCHAR(32) PRIMARY KEY NOT NULL,"
            + "value INTEGER NOT NULL)";
    }

    @Override
    String getQueryGetTable() {
        return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=? AND TABLE_SCHEMA='"+ GlobalConfig.databaseMySqlDatabase+"'";
    }
}
