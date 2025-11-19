package com.pfa.pfaproject.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * Loads environment variables from .env file in development.
 * In production (Render), environment variables are set via dashboard
 * and this will be skipped.
 */
@Configuration
public class EnvironmentConfig {

    @PostConstruct
    public void loadEnvironmentVariables() {
        try {
            // Load .env file if it exists (development)
            // In production (Render), this will be ignored
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()  // Don't fail if .env doesn't exist
                    .load();
            
            // Set system properties from .env file
            dotenv.entries().forEach(entry -> {
                // Only set if not already set by system/Render
                if (System.getProperty(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
            
            System.out.println("✅ Environment variables loaded from .env file");
        } catch (Exception e) {
            // Silent fail - .env is optional (Render sets vars directly)
            System.out.println("ℹ️  No .env file found - using system environment variables");
        }
    }
}

