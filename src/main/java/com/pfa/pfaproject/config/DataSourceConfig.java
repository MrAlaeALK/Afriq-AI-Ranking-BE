package com.pfa.pfaproject.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;

/**
 * DataSource configuration that handles both JDBC and postgres:// URL formats.
 * Converts Render's postgres:// URL format to jdbc:postgresql:// format.
 */
@Configuration
public class DataSourceConfig {

    @Autowired
    private Environment environment;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        // Read DATABASE_URL from environment (Render sets this)
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null) {
            databaseUrl = System.getenv("DATABASE_URL");
        }
        
        // If DATABASE_URL is provided and starts with postgres://, convert it
        if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("postgres://")) {
            try {
                // Parse postgres:// URL
                URI dbUri = new URI(databaseUrl);
                
                // Extract user info (username:password)
                String userInfo = dbUri.getUserInfo();
                if (userInfo == null) {
                    throw new IllegalArgumentException("DATABASE_URL missing user info");
                }
                
                // Split username and password
                int colonIndex = userInfo.indexOf(':');
                if (colonIndex == -1) {
                    throw new IllegalArgumentException("DATABASE_URL user info format invalid");
                }
                
                String username = userInfo.substring(0, colonIndex);
                String password = userInfo.substring(colonIndex + 1);
                
                // URL decode password in case it's encoded
                password = java.net.URLDecoder.decode(password, java.nio.charset.StandardCharsets.UTF_8);
                
                String host = dbUri.getHost();
                int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
                String database = dbUri.getPath();
                if (database != null && database.startsWith("/")) {
                    database = database.substring(1);
                }
                
                // Convert to JDBC URL format
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                
                // Create HikariCP configuration
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
                
                // Connection pool settings
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                
                System.out.println("✅ Converted postgres:// URL to JDBC format");
                System.out.println("   Host: " + host + ":" + port);
                System.out.println("   Database: " + database);
                
                return new HikariDataSource(config);
            } catch (Exception e) {
                System.err.println("❌ Error converting DATABASE_URL: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to convert DATABASE_URL from postgres:// to jdbc:postgresql:// format", e);
            }
        }
        
        // Fall back to default Spring Boot DataSource configuration
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}

