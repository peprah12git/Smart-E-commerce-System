package com.ecommerce.config;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Database configuration with HikariCP connection pooling.
 * Manages database connections efficiently with connection pooling.
 */
public class DatabaseConfig {
    private static HikariDataSource dataSource;
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "noah_1@23.Djanor";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection pool configuration
    private static final int MAX_POOL_SIZE = 10;
    private static final int MIN_IDLE_CONNECTIONS = 5;
    private static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final int IDLE_TIMEOUT = 600000; // 10 minutes
    private static final int MAX_LIFETIME = 1800000; // 30 minutes
    
    /**
     * Initialize the connection pool
     */
    static {
        try {
            Class.forName(DB_DRIVER);
            initializeDataSource();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
    
    /**
     * Create and configure HikariCP data source
     */
    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setMinimumIdle(MIN_IDLE_CONNECTIONS);
        config.setConnectionTimeout(CONNECTION_TIMEOUT);
        config.setIdleTimeout(IDLE_TIMEOUT);
        config.setMaxLifetime(MAX_LIFETIME);
        config.setAutoCommit(true);
        config.setPoolName("EcommerceHikariPool");
        
        dataSource = new HikariDataSource(config);
        System.out.println("Database connection pool initialized successfully!");
    }
    
    /**
     * Get a connection from the pool
     * @return Connection object
     * @throws SQLException if unable to get a connection
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource not initialized!");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Close the connection pool
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Connection pool closed.");
        }
    }
    
    /**
     * Get HikariCP data source
     * @return HikariDataSource
     */
    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
