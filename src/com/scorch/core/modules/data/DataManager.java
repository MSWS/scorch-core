package com.scorch.core.modules.data;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import com.google.gson.Gson;
import com.scorch.annotations.DataIgnore;
import com.scorch.annotations.DataNotNull;
import com.scorch.core.modules.AbstractModule;
import com.scorch.utils.Logger;

public class DataManager extends AbstractModule {

	private static Gson gson = new Gson();

	private ConnectionManager connectionManager;

	public DataManager(String id, ConnectionManager connectionManager) {
		super(id);
		this.connectionManager = connectionManager;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub

	}

	/**
	 * Creates a table if it doesn't exist already with <code>name</code> as name
	 * and with the variables of <code>storageType</code> as columns Call this in
	 * your plugin/module initialisation
	 * 
	 * @param name        the name of the database
	 * @param storageType the column template
	 */
	public void createTable(String name, Object storageType) {

		String query = "CREATE TABLE IF NOT EXISTS " + name + " (table_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, ";

		for (int i = 0; i < storageType.getClass().getFields().length; i++) {
			Field field = storageType.getClass().getFields()[i];

			// Makes sure that the field doesnt have to be ignored for serialisation
			if (field.getAnnotation(DataIgnore.class) != null)
				continue;

			query += field.getName();

			if (field.getType() == Integer.class) {
				query += " INT ";
			} else if (field.getType() == String.class) {
				query += " TEXT ";
			} else if (field.getType() == Long.class) {
				query += " BIGINT ";
			} else if (field.getType() == UUID.class) {
				query += " VARCHAR(36) ";
			} else if (field.getType().isEnum()) {
				query += " TEXT ";
			} else if (Collection.class.isAssignableFrom(field.getClass())) {
				query += " TEXT ";
			} else if (Map.class.isAssignableFrom(field.getClass())) {
				query += " TEXT ";
			} else if (field.getType() == Location.class) {
				query += " TEXT ";
			} else {
				query += " TEXT ";
			}

			if (field.getAnnotation(DataNotNull.class) != null) {
				query += "NOT NULL, ";
			} else if (i != storageType.getClass().getFields().length) {
				query += ", ";
			} else {
				query += ");";
			}

			this.getConnectionManager().executeQuery(query);

		}
	}

	/**
	 * Saves the <code>object</code> to <code>table</code>
	 * 
	 * @param table  the table to save <code>object</code> to
	 * @param object the object to save
	 */
	public void saveObject(String table, Object object) {
		// Create prepared statement based on the object
		String query = "INSERT INTO " + table + " (";
		for (int i = 0; i < object.getClass().getFields().length; i++) {
			Field field = object.getClass().getFields()[i];

			if (field.getAnnotation(DataIgnore.class) == null) {
				query += field.getName();

				if (i == object.getClass().getFields().length) {
					query += ") VALUES (";
					for (int j = 0; j < object.getClass().getFields().length; j++) {
						query += "?";
						if (j == object.getClass().getFields().length) {
							query += ");";
						} else {
							query += ", ";
						}
					}
				} else {
					query += ", ";
				}
			}
		}

		// Create prepared statement instance

		try {
			PreparedStatement statement = this.getConnectionManager().prepareStatement(query);
			for (int i = 0; i < object.getClass().getFields().length; i++) {
				Field field = object.getClass().getFields()[i];

				try {
					if (field.getType() == Integer.class) {
						statement.setInt(i + 1, (int) field.get(object));
					} else if (field.getType() == String.class) {
						statement.setString(i + 1, (String) field.get(object));
					} else if (field.getType() == Long.class) {
						statement.setLong(i + 1, (long) field.get(object));
					} else if (field.getType() == UUID.class) {
						statement.setString(i + 1, ((UUID) field.get(object)).toString());
					} else if (field.getType().isEnum()) {
						statement.setString(i + 1, field.get(object).toString());
					} else if (Collection.class.isAssignableFrom(field.getClass())) {
						statement.setString(i + 1, DataManager.getGson().toJson(field.get(object)));
					} else if (Map.class.isAssignableFrom(field.getClass())) {
						statement.setString(i + 1, DataManager.getGson().toJson(field.get(object)));
					} else if (field.getType() == Location.class) {
						statement.setString(i + 1, DataManager.getGson().toJson(field.get(object)));
					} else {
						statement.setString(i + 1, DataManager.getGson().toJson(field.get(object)));
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Logger.error("An error occured while trying to serialize a class for a prepared statement: "
							+ e.getMessage() + "\n" + query);
				}
			}
			statement.execute();
		} catch (SQLException e) {
			Logger.error("An error occured while trying to save an object:" + e.getMessage());
		}

	}

	/**
	 * Gets the gson instance
	 * 
	 * @return the gson instance
	 */
	public static Gson getGson() {
		return gson;
	}

	/**
	 * Gets the connectionmanager
	 * 
	 * @return the connection manager
	 */
	private ConnectionManager getConnectionManager() {
		return this.connectionManager;
	}
}
