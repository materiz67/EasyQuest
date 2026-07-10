package pl.materiz66.easyquests.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteDatabase {
    private HikariDataSource dataSource;

    public void connect(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + new File(dataFolder, "database.db").getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");

        config.setMaximumPoolSize(1); // SQLite bezpiecznie obsługuje tylko 1 wątek zapisu
        config.setPoolName("EasyQuests-SQLite");

        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Base danych SQLite nie jest polaczona!");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}