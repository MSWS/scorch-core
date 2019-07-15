package com.scorch.core.modules.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.bukkit.Location;

import com.google.gson.Gson;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.modules.data.annotations.DataNotNull;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;

/**
 * Utility to easily save different types of objects to a database and load them
 *
 * @author Gijs de Jong
 */
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
	 * and with the fields of <code>storageType</code> as columns Call this in your
	 * plugin/module initialisation
	 * 
	 * @param name        the name of the database
	 * @param storageType the column template
	 */
	public void createTable(String name, Class<?> storageType) throws NoDefaultConstructorException {

		if (!hasDefaultConstructor(storageType)) {
			throw new NoDefaultConstructorException();
		}

		String query = "CREATE TABLE IF NOT EXISTS " + name
				+ " (local_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, object_type TEXT NOT NULL, ";

		for (int i = 0; i < storageType.getDeclaredFields().length; i++) {
			Field field = storageType.getDeclaredFields()[i];

			// Makes sure that the field doesn't have to be ignored for serialization
			if (field.isAnnotationPresent(DataIgnore.class))
				continue;

			query += field.getName();

			if (field.getType() == Integer.class) {
				query += " INT";
			} else if (field.getType() == String.class) {
				query += " TEXT";
			} else if (field.getType() == long.class) {
				query += " BIGINT";
			} else if (field.getType() == UUID.class) {
				query += " VARCHAR(36)";
			} else if (field.getType().isEnum()) {
				query += " TEXT";
			} else if (Collection.class.isAssignableFrom(field.getType())) {
				query += " TEXT";
			} else if (Map.class.isAssignableFrom(field.getType())) {
				query += " TEXT";
			} else if (field.getType() == Location.class) {
				query += " TEXT";
			} else {
				query += " TEXT";
			}

			if (field.getAnnotation(DataNotNull.class) != null) {
				query += " NOT NULL";
			}

			if (i != (storageType.getDeclaredFields().length - 1)) {
				query += ", ";
			} else {
				query += ");";
				Logger.log(query);
				this.getConnectionManager().executeQuery(query);
			}
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
		String query = "INSERT INTO " + table + " (object_type, ";
		for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {
			Field field = object.getClass().getDeclaredFields()[i];

			// Makes sure that the field doesn't have to be ignored for serialisation
			if (field.isAnnotationPresent(DataIgnore.class))
				continue;

			if (field.getAnnotation(DataIgnore.class) == null) {
				query += field.getName();

				if (i == object.getClass().getDeclaredFields().length - 1) {
					query += ") VALUES (?,";
					for (int j = 0; j < object.getClass().getDeclaredFields().length; j++) {
						if (object.getClass().getDeclaredFields()[j].isAnnotationPresent(DataIgnore.class))
							continue;
						query += "?";
						if (j == object.getClass().getDeclaredFields().length - 1) {
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

		// Create a prepared statement and populate it
		try {
			PreparedStatement statement = this.getConnectionManager().prepareStatement(query);
			statement.setString(1, object.getClass().getName());

			int parameterIndex = 2;

			for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {
				Field field = object.getClass().getDeclaredFields()[i];

				// Make sure the field is accessible
				field.setAccessible(true);

				// Makes sure that the field doesn't have to be ignored for serialisation
				if (field.isAnnotationPresent(DataIgnore.class))
					continue;

				try {
					if (field.getType() == Integer.class) {
						statement.setInt(parameterIndex, (int) field.get(object));
					} else if (field.getType() == String.class) {
						statement.setString(parameterIndex, (String) field.get(object));
					} else if (field.getType() == long.class) {
						statement.setLong(parameterIndex, (long) field.get(object));
					} else if (field.getType() == UUID.class) {
						statement.setString(parameterIndex, ((UUID) field.get(object)).toString());
					} else if (field.getType().isEnum()) {
						statement.setString(parameterIndex, field.get(object).toString());
					} else if (Collection.class.isAssignableFrom(field.getType())) {
						statement.setString(parameterIndex, DataManager.getGson().toJson(field.get(object)));
					} else if (Map.class.isAssignableFrom(field.getType())) {
						statement.setString(parameterIndex, DataManager.getGson().toJson(field.get(object)));
					} else if (field.getType() == Location.class) {
						// TODO Write JSON wrapper for Location
						statement.setString(parameterIndex, DataManager.getGson().toJson(field.get(object)));
					} else {
						statement.setString(parameterIndex, DataManager.getGson().toJson(field.get(object)));
					}

					parameterIndex++;

				} catch (IllegalArgumentException | IllegalAccessException e) {
					Logger.error("An error occurred while trying to serialize a class for a prepared statement " + "("
							+ field.getName() + " of " + field.getDeclaringClass().getName() + "): " + e.getMessage()
							+ "\n" + query);
				}
			}
			Logger.log(statement.toString());
			statement.execute();
		} catch (SQLException e) {
			Logger.error("An error occurred while trying to save an object: " + e.getMessage());
		}
	}

	/**
	 * Gets the object where selector == value from table Returns a
	 * {@link java.util.Collection} of said object if multiple objects are found in
	 * the database that match the selectors
	 * 
	 * @param table        the table
	 * @param sqlSelectors the selectors to use
	 * @return the object found in the database
	 */
	public Object getObject(String table, SQLSelector... sqlSelectors) throws DataObtainException {

		if (table == null || table.equals(""))
			throw new DataObtainException("Table name is null");
		if (sqlSelectors == null || sqlSelectors.length == 0)
			throw new DataObtainException("No sql selectors defined");

		String sql = String.format("SELECT * FROM %s WHERE %s='%s' ", table, sqlSelectors[0].getSelector(),
				sqlSelectors[0].getValue());

		// Strip first element from sqlSelectors because we just used it ^ there
		SQLSelector[] remainingSelectors = IntStream.range(1, sqlSelectors.length).mapToObj(i -> sqlSelectors[i])
				.toArray(SQLSelector[]::new);

		for (int i = 0; i < remainingSelectors.length; i++) {
			sql = sql + String.format(" AND %s='&s'", remainingSelectors[i].getSelector(),
					remainingSelectors[i].getValue());
		}

		sql = sql + ";";

		ResultSet res = this.getConnectionManager().executeQuery(sql);

		try {
			if (res == null)
				return null;
			res.last();
			int rowCount = res.getRow();

			// Reset result set iterator
			res.beforeFirst();

			Collection<Object> collection = new ArrayList<>();

			while (res.next()) {
				String className = res.getString("object_type");
				Class<?> clazz = Class.forName(className);
				Object dataObject = clazz.newInstance();

				// Start at 3 since columns start counting at 1 and we need to skip the first
				// two (local_id and object_type)
				int columnIndex = 3;

				// Start at one since we've already gotten the class name ^
				for (Field field : clazz.getDeclaredFields()) {

					// Make sure the field doesn't have to be ignored
					if (field.isAnnotationPresent(DataIgnore.class))
						continue;

					// Make sure the field is accessible in case it's private
					field.setAccessible(true);

					if (field.getType() == Integer.class) {
						field.set(dataObject, res.getInt(columnIndex));
					} else if (field.getType() == String.class) {
						field.set(dataObject, res.getString(columnIndex));
					} else if (field.getType() == long.class) {
						field.set(dataObject, res.getLong(columnIndex));
					} else if (field.getType() == UUID.class) {
						field.set(dataObject, UUID.fromString(res.getString(columnIndex)));
					} else if (field.getType().isEnum()) {
						field.set(dataObject, field.getType().getMethod("valueOf", String.class).invoke(null,
								res.getString(columnIndex)));
					} else if (Collection.class.isAssignableFrom(field.getType())) {
						field.set(dataObject, getGson().fromJson(res.getString(columnIndex), Collection.class));
					} else if (Map.class.isAssignableFrom(field.getType())) {
						field.set(dataObject, getGson().fromJson(res.getString(columnIndex), Map.class));
					} else if (field.getType() == Location.class) {
						// TODO Write JSON wrapper for Location
						field.set(dataObject, getGson().fromJson(res.getString(columnIndex), Location.class));
					} else {
						field.set(dataObject, field.getType().cast(res.getObject(columnIndex)));
					}

					columnIndex++;
				}
				if (rowCount > 1) {
					// should return array so add to list
					collection.add(dataObject);
				} else {
					return dataObject;
				}
			}
			return collection;
		} catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException
				| NoSuchMethodException | InvocationTargetException e) {
			Logger.error("An error occurred while trying to get object from table: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets all the objects in a table using SELECT * FROM table
	 * 
	 * @param table the target table
	 * @return a {@link Collection} of objects that are stored in the table
	 * @throws DataObtainException thrown when there's an issue obtaining the data
	 *
	 * @see this.getObject
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getAllObjects(String table) throws DataObtainException {
		if (table == null || table == "")
			throw new DataObtainException("Table name is null");
		ResultSet res = this.getConnectionManager().executeQuery(String.format("SELECT * FROM %s;", table));
		Collection<T> collection = new ArrayList<>();

		try {
			if (res == null)
				return collection;
			while (res.next()) {
				String className = res.getString("object_type");
				Class<?> clazz = Class.forName(className);
				Object dataObject = clazz.newInstance();

				// Start at 3 since columns start counting at 1 and we need to skip the first
				// two (local_id and object_type)
				int columnIndex = 3;

				// Start at one since we've already gotten the class name ^
				for (Field field : clazz.getDeclaredFields()) {

					// Make sure the field doesn't have to be ignored
					if (field.isAnnotationPresent(DataIgnore.class))
						continue;

					// Make sure the field is accessible in case it's private
					field.setAccessible(true);

					if (field.getType() == Integer.class) {
						field.set(dataObject, res.getInt(columnIndex));
					} else if (field.getType() == String.class) {
						field.set(dataObject, res.getString(columnIndex));
					} else if (field.getType() == long.class) {
						field.set(dataObject, res.getLong(columnIndex));
					} else if (field.getType() == UUID.class) {
						field.set(dataObject, UUID.fromString(res.getString(columnIndex)));
					} else if (field.getType().isEnum()) {
						field.set(dataObject, field.getType().getMethod("valueOf", String.class).invoke(null,
								res.getString(columnIndex)));
					} else if (Collection.class.isAssignableFrom(field.getType())) {
						field.set(dataObject, getGson().fromJson(res.getString(columnIndex), Collection.class));
					} else if (Map.class.isAssignableFrom(field.getType())) {
						field.set(dataObject, getGson().fromJson(res.getString(columnIndex), Map.class));
					} else if (field.getType() == Location.class) {
						// TODO Write JSON wrapper for Location
						field.set(dataObject, getGson().fromJson(res.getString(columnIndex), Location.class));
					} else {
						field.set(dataObject, field.getType().cast(res.getObject(columnIndex)));
					}

					columnIndex++;
				}
				collection.add((T) dataObject);
			}
			return collection;
		} catch (SQLException | IllegalAccessException | NoSuchMethodException | InstantiationException
				| ClassNotFoundException | InvocationTargetException e) {
			Logger.error("An error occurred while trying to get objects from table: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Checks if the type has a default constructor
	 * 
	 * @param type the type to check
	 * @return whether the type has a default constructor
	 */
	private boolean hasDefaultConstructor(Class<?> type) {
		// Loop through all constructors in type
		for (Constructor<?> constructor : type.getConstructors()) {
			// No parameters in constructor so its a default constructor
			if (constructor.getParameters().length == 0) {
				return true;
			}
		}
		// No default constructor found
		return false;
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
