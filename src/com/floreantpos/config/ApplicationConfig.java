package com.floreantpos.config;

import static com.floreantpos.config.ApplicationConfig.DATABASE_NAME;
import static com.floreantpos.config.ApplicationConfig.DATABASE_PASSWORD;
import static com.floreantpos.config.ApplicationConfig.DATABASE_PORT;
import static com.floreantpos.config.ApplicationConfig.DATABASE_URL;
import static com.floreantpos.config.ApplicationConfig.DATABASE_USER;
import static com.floreantpos.config.ApplicationConfig.checkDatabaseConnection;
import static com.floreantpos.config.ApplicationConfig.getDatabaseName;
import static com.floreantpos.config.ApplicationConfig.getDatabasePassword;
import static com.floreantpos.config.ApplicationConfig.getDatabasePort;
import static com.floreantpos.config.ApplicationConfig.getDatabaseURL;
import static com.floreantpos.config.ApplicationConfig.getDatabaseUser;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.prefs.Preferences;
import com.mysql.jdbc.Driver;

import com.floreantpos.main.Application;

public class ApplicationConfig {
	public static final String DATABASE_URL = "DATABASE_URL";
	public static final String DATABASE_PORT = "DATABASE_PORT";
	public static final String DATABASE_NAME = "DATABASE_NAME";
	public static final String DATABASE_USER = "DATABASE_USER";
	public static final String DATABASE_PASSWORD = "DATABASE_PASS";

	private final static Preferences pref = Preferences.userNodeForPackage(Application.class);

	public static int getTerminalId() {
		return pref.getInt("TERMINAL_ID", -1);
	}

	public static void setTerminalId(int id) {
		pref.putInt("TERMINAL_ID", id);
	}

	public static Preferences getPreferences() {
		return pref;
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		return pref.getBoolean(key, defaultValue);
	}

	public static String getString(String key) {
		return pref.get(key, null);
	}

	public static String getString(String key, String defaultValue) {
		return pref.get(key, defaultValue);
	}

	public static void put(String key, boolean value) {
		pref.putBoolean(key, value);
	}

	public static void put(String key, String value) {
		pref.put(key, value);
	}

	public static String getDatabaseURL() {
		return pref.get(DATABASE_URL, "localhost");
	}

	public static String getConnectionURL() {
        return "jdbc:mysql://" + getDatabaseURL() + ":" + getDatabasePort() + "/" + getDatabaseName() + "?characterEncoding=UTF-8";
	}

	public static void setDatabaseURL(String url) {
		pref.put(DATABASE_URL, url);
	}

	public static String getDatabasePort() {
		return pref.get(DATABASE_PORT, "3306");
	}

	public static void setDatabasePort(String port) {
		pref.put(DATABASE_PORT, port);
	}

	public static String getDatabaseName() {
		return pref.get(DATABASE_NAME, "posdb");
	}

	public static void setDatabaseName(String name) {
		pref.put(DATABASE_NAME, name);
	}

	public static String getDatabaseUser() {
		return pref.get(DATABASE_USER, "app");
	}

	public static void setDatabaseUser(String user) {
		pref.put(DATABASE_USER, user);
	}

	public static String getDatabasePassword() {
		return pref.get(DATABASE_PASSWORD, "sa");
	}

	public static void setDatabasePassword(String password) {
		pref.put(DATABASE_PASSWORD, password);
	}

	public static boolean checkDatabaseConnection(String url, String port, String databaseName, String user, String password) {
        url = "jdbc:mysql://" + url + ":" + port + "/" + databaseName + "?characterEncoding=UTF-8";

		Connection connection = null;
		try {
            new Driver();
			connection = DriverManager.getConnection(url, user, password);
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			try {
				connection.close();
			}catch(Throwable t) {}
		}
	}
	public static boolean checkDatabaseConnection() {
		return checkDatabaseConnection(getDatabaseURL(), getDatabasePort(), getDatabaseName(), getDatabaseUser(), getDatabasePassword());
	}
}
