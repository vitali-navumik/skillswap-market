package com.vitali.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final String APPLICATION_PROPERTIES_PATH = "application.properties";
    private static final String APPLICATION_LOCAL_PROPERTIES_PATH = "application.local.properties";

    public static final String API_URL;
    public static final String UI_BASE_URL;
    public static final String TOKEN_SECRET;
    public static final int TOKEN_EXPIRATION_DAYS;

    public static final String DB_URL;
    public static final String DB_USER;
    public static final String DB_PASSWORD;
    public static final String DB_NAME;
    public static final String DEFAULT_USER_PASSWORD;

    public static final String ADMIN_EMAIL;
    public static final String ADMIN_PASSWORD;

    static {
        try (InputStream baseInput = Config.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES_PATH);
             InputStream localInput = Config.class.getClassLoader().getResourceAsStream(APPLICATION_LOCAL_PROPERTIES_PATH)) {
            Properties baseProp = new Properties();
            Properties localProp = new Properties();

            if (baseInput == null) {
                throw new RuntimeException("Sorry, unable to find " + APPLICATION_PROPERTIES_PATH);
            }

            baseProp.load(baseInput);

            if (localInput != null) {
                localProp.load(localInput);
            }

            API_URL = System.getenv("API_URL") != null
                    ? System.getenv("API_URL")
                    : localProp.getProperty("api.url", baseProp.getProperty("api.url"));
            UI_BASE_URL = System.getenv("UI_BASE_URL") != null
                    ? System.getenv("UI_BASE_URL")
                    : localProp.getProperty("ui.url", baseProp.getProperty("ui.url"));
            TOKEN_SECRET = System.getenv("TOKEN_SECRET") != null
                    ? System.getenv("TOKEN_SECRET")
                    : localProp.getProperty("token.secret", baseProp.getProperty("token.secret"));
            TOKEN_EXPIRATION_DAYS = System.getenv("TOKEN_EXPIRATION_DAYS") != null
                    ? Integer.parseInt(System.getenv("TOKEN_EXPIRATION_DAYS"))
                    : Integer.parseInt(localProp.getProperty("token.expiration.days", baseProp.getProperty("token.expiration.days")));

            DB_URL = System.getenv("DB_URL") != null
                    ? System.getenv("DB_URL")
                    : localProp.getProperty("db.url", baseProp.getProperty("db.url"));
            DB_USER = System.getenv("DB_USER") != null
                    ? System.getenv("DB_USER")
                    : localProp.getProperty("db.user", baseProp.getProperty("db.user"));
            DB_PASSWORD = System.getenv("DB_PASSWORD") != null
                    ? System.getenv("DB_PASSWORD")
                    : localProp.getProperty("db.password", baseProp.getProperty("db.password"));
            DB_NAME = System.getenv("DB_NAME") != null
                    ? System.getenv("DB_NAME")
                    : localProp.getProperty("db.name", baseProp.getProperty("db.name"));
            DEFAULT_USER_PASSWORD = System.getenv("USER_PASSWORD") != null
                    ? System.getenv("USER_PASSWORD")
                    : localProp.getProperty("user.password", baseProp.getProperty("user.password"));

            ADMIN_EMAIL = System.getenv("ADMIN_EMAIL") != null
                    ? System.getenv("ADMIN_EMAIL")
                    : localProp.getProperty("admin.email", baseProp.getProperty("admin.email"));
            ADMIN_PASSWORD = System.getenv("ADMIN_PASSWORD") != null
                    ? System.getenv("ADMIN_PASSWORD")
                    : localProp.getProperty("admin.password", baseProp.getProperty("admin.password"));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load application properties", ex);
        }
    }
}
