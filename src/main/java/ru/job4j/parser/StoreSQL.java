package ru.job4j.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Vacancy storage.
 *
 * @author IvanPJF (teaching-light@yandex.ru)
 * @version 0.1
 * @since 24.06.2019
 */
public class StoreSQL implements AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(StoreSQL.class.getName());

    private Connection connection;
    private Config appConfig;
    private Config queryConfig = new Config("sql.properties");
    private boolean insertCommit = true;

    public StoreSQL(Config config) {
        this.appConfig = config;
        this.init();
    }

    public StoreSQL(Connection connection) {
        this.insertCommit = false;
        this.connection = connection;
        this.createTable();
    }

    /**
     * Database initialization.
     */
    public void init() {
        try {
            Class.forName(this.appConfig.get("jdbc.driver"));
            this.connection = DriverManager.getConnection(
                    this.appConfig.get("jdbc.url"),
                    this.appConfig.get("jdbc.username"),
                    this.appConfig.get("jdbc.password")
            );
            LOG.trace("The database connection was successful!");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        this.createTable();
    }

    /**
     * Create a table if it does not exist.
     */
    private void createTable() {
        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(this.queryConfig.get("create.table"));
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Inserting new vacancies in the table.
     *
     * @param vacancies Set of vacancy to insert.
     */
    public int insert(Set<Vacancy> vacancies) {
        int result = 0;
        try (PreparedStatement pdStmt =
                     this.connection.prepareStatement(this.queryConfig.get("insert.table"))) {
            this.connection.setAutoCommit(false);
            for (Vacancy value : vacancies) {
                pdStmt.setString(1, value.getName());
                pdStmt.setString(2, value.getText());
                pdStmt.setString(3, value.getLink());
                pdStmt.setTimestamp(4, Timestamp.valueOf(value.getDateCreate()));
                pdStmt.addBatch();
            }
            result = pdStmt.executeBatch().length;
            if (this.insertCommit) {
                this.connection.commit();
                this.connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException ex) {
                LOG.error(String.format("Error rollback method; %s", e.getMessage()), e);
            }
            LOG.error(String.format("Error insert method; %s", e.getMessage()), e);
        }
        return result;
    }

    /**
     * Get the date of the most recent vacancy.
     * When you first start the program is given "current date - 1 year".
     *
     * @return
     */
    public LocalDateTime getLastDate() {
        String date = null;
        try (ResultSet rs = this.connection.createStatement()
                .executeQuery(this.queryConfig.get("select.lastdate.table"))) {
            rs.next();
            date = rs.getString("last_date");
        } catch (SQLException e) {
            LOG.error(String.format("Error getLastDate method; %s", e.getMessage()), e);
        }
        return date != null
                ? Timestamp.valueOf(date).toLocalDateTime()
                : LocalDateTime.now().minusYears(1);
    }

    public boolean isConnected() {
        return this.connection != null;
    }

    @Override
    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
