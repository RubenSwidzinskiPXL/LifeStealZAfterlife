package com.zetaplugins.lifestealz.storage;

import com.zetaplugins.lifestealz.LifeStealZ;
import com.zetaplugins.lifestealz.storage.connectionPool.ConnectionPool;
import com.zetaplugins.lifestealz.storage.connectionPool.SQLiteConnectionPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public final class SQLiteStorage extends SQLStorage {
    private final SQLiteConnectionPool connectionPool;

    public SQLiteStorage(LifeStealZ plugin) {
        super(plugin);
        connectionPool = new SQLiteConnectionPool(getPlugin().getDataFolder().getPath() + "/userData.db");
    }

    @Override
    protected void migrateDatabase() {
        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("PRAGMA table_info(hearts)")
        ) {
            boolean hasFirstJoin = false;
            boolean hasLifeState = false;
            boolean hasAfterlifeReleaseTime = false;

            while (resultSet.next()) {
                String columnName = resultSet.getString("name");
                if ("firstJoin".equalsIgnoreCase(columnName)) {
                    hasFirstJoin = true;
                } else if ("lifeState".equalsIgnoreCase(columnName)) {
                    hasLifeState = true;
                } else if ("afterlifeReleaseTime".equalsIgnoreCase(columnName)) {
                    hasAfterlifeReleaseTime = true;
                }
            }

            if (!hasFirstJoin) {
                getPlugin().getLogger().info("Adding 'firstJoin' column to 'hearts' table.");
                statement.executeUpdate("ALTER TABLE hearts ADD COLUMN firstJoin INTEGER DEFAULT 0");
            }
            
            if (!hasLifeState) {
                getPlugin().getLogger().info("Adding 'lifeState' column to 'hearts' table.");
                statement.executeUpdate("ALTER TABLE hearts ADD COLUMN lifeState TEXT DEFAULT 'ALIVE'");
            }
            
            if (!hasAfterlifeReleaseTime) {
                getPlugin().getLogger().info("Adding 'afterlifeReleaseTime' column to 'hearts' table.");
                statement.executeUpdate("ALTER TABLE hearts ADD COLUMN afterlifeReleaseTime INTEGER DEFAULT 0");
            }
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Failed to migrate database: ", e);
        }
    }

    @Override
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    @Override
    protected String getInserOrReplaceStatement() {
        return "INSERT OR REPLACE INTO hearts (uuid, name, maxhp, hasbeenRevived, craftedHearts, craftedRevives, killedOtherPlayers, firstJoin, lifeState, afterlifeReleaseTime) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }
}
