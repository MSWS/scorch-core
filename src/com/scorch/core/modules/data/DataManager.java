package com.scorch.core.modules.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.modules.data.annotations.DataNotNull;
import com.scorch.core.modules.data.exceptions.DataDeleteException;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.DataUpdateException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.modules.data.wrappers.JSONLocation;
import com.scorch.core.utils.Logger;

/*
 * Utility to easily save different types of objects to a database and load them
 *
 * @author Gijs de Jong
 */
public class DataManager extends AbstractModule {

	// Maybe change the gson configuration sometime in the future.
	private static Gson gson = new GsonBuilder().create();

	private ConnectionManager connectionManager;

	private Map<OfflinePlayer, CPlayer> players;

	public DataManager(String id, ConnectionManager connectionManager) {
		super(id);
		this.connectionManager = connectionManager;
	}

	@Override
	public void initialize() {
		players = new HashMap<>();
	}

	@Override
	public void disable() {
	}

	public CPlayer getPlayer(OfflinePlayer player) {
		if (!players.containsKey(player))
			players.put(player, new CPlayer(player));
		return players.get(player);
	}

	public ArrayList<OfflinePlayer> getLoadedPlayers() {
		return new ArrayList<OfflinePlayer>(players.keySet());
	}

	public void removePlayer(OfflinePlayer player) {
		players.remove(player);
	}

	public void clearPlayers() {
		for (OfflinePlayer player : players.keySet())
			removePlayer(player);
	}

	public void loadData(OfflinePlayer player) {
		if (players.containsKey(player))
			throw new IllegalArgumentException("Player data already loaded");
		players.put(player, new CPlayer(player));
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

		Logger.log("Ensuring %s table exists", name);

		if (!hasDefaultConstructor(storageType)) {
			throw new NoDefaultConstructorException();
		}

		String query = "CREATE TABLE IF NOT EXISTS " + name
				+ " (local_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, object_type TEXT NOT NULL, ";

		for (int i = 0; i < storageType.getDeclaredFields().length; i++) {
			Field field = storageType.getDeclaredFields()[i];

			// Makes sure that the field doesn't have to be ignored for serialization
			if (field.isAnnotationPresent(DataIgnore.class)) {
				if (i == storageType.getDeclaredFields().length - 1) {
					if (query.endsWith(", ")) {
						query = query.substring(0, query.length() - 2);
					}
					query += ");";
					this.getConnectionManager().executeQuery(query);
				}
				continue;
			}

			query += field.getName();

			if (field.getType() == Integer.class) {
				query += " INT";
			} else if (field.getType() == String.class) {
				query += " TEXT";
			} else if (field.getType() == boolean.class) {
				query += " BOOLEAN ";
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
				this.getConnectionManager().executeQuery(query);
			}
		}
	}

	/**
	 * Saves the bject to table, automatically parses all fields of the object to
	 * their sql equivalent. <br>
	 * Supported field types include:</b> <br>
	 * <ul>
	 * <li>{@link Integer}</li>
	 * <li>{@link String}</li>
	 * <li>{@link Boolean}</li>
	 * <li>{@link Long}</li>
	 * <li>{@link UUID}</li>
	 * <li>{@link Location}</li>
	 * <li>{@link Collection}</li>
	 * <li>{@link Map}</li>
	 * </ul>
	 * </br>
	 * <br>
	 * If the type isn't listed above, it will try to convert the object to json
	 * using Google's {@link Gson}</br>
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
					} else if (field.getType() == boolean.class) {
						statement.setBoolean(parameterIndex, (boolean) field.get(object));
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
						statement.setString(parameterIndex,
								DataManager.getGson().toJson(JSONLocation.fromLocation((Location) field.get(object))));
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
			statement.execute();
		} catch (SQLException e) {
			Logger.error("An error occurred while trying to save an object: " + e.getMessage());
		}
	}

	/**
	 * Saves the bject to table, automatically parses all fields of the object to
	 * their sql equivalent. <br>
	 * Supported field types include:</b> <br>
	 * <ul>
	 * <li>{@link Integer}</li>
	 * <li>{@link String}</li>
	 * <li>{@link Boolean}</li>
	 * <li>{@link UUID}</li>
	 * <li>{@link Location}</li>
	 * <li>{@link Collection}</li>
	 * <li>{@link Map}</li>
	 * </ul>
	 * </br>
	 * <br>
	 * If the type isn't listed above, it will try to convert the object to json
	 * using Google's {@link Gson}</br>
	 * 
	 * @param table  the table to save <code>object</code> to
	 * @param object the object to save
	 */
	public void saveObjectAsync(String table, Object object) {
		new BukkitRunnable() {
			@Override
			public void run() {
				saveObject(table, object);
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
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
					} else if (field.getType() == boolean.class) {
						field.set(dataObject, res.getBoolean(columnIndex));
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
						field.set(dataObject,
								getGson().fromJson(res.getString(columnIndex), JSONLocation.class).toBukkitLocation());
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
					} else if (field.getType() == boolean.class) {
						field.set(dataObject, res.getBoolean(columnIndex));
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
						field.set(dataObject,
								getGson().fromJson(res.getString(columnIndex), JSONLocation.class).toBukkitLocation());
					} else {
						field.set(dataObject, field.getType().cast(res.getObject(columnIndex)));
					}

					columnIndex++;
				}

				if (dataObject.getClass() != clazz) {
					Logger.warn("OBJECT ISN'T A %s", clazz);
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
	 * Deletes the object specified using the {@link SQLSelector}s from the table
	 * 
	 * @param table        the table to delete the data from
	 * @param sqlSelectors the sql selectors that specify the data
	 * @throws DataDeleteException
	 *
	 * @see SQLSelector
	 */
	public void deleteObject(String table, SQLSelector... sqlSelectors) throws DataDeleteException {
		if (table == null || table.equals(""))
			throw new DataDeleteException("Table name is null");
		if (sqlSelectors == null || sqlSelectors.length == 0)
			throw new DataDeleteException("No sql selectors defined");

		// TODO prepared statement
		String sql = String.format("DELETE FROM %s WHERE %s=? ", table, sqlSelectors[0].getSelector(),
				sqlSelectors[0].getValue());

		// Strip first element from sqlSelectors because we just used it ^ there

		for (int i = 1; i < sqlSelectors.length; i++) {
			sql = sql + String.format(" AND %s=?", sqlSelectors[i].getSelector(), sqlSelectors[i].getValue());
		}

		sql = sql + ";";

		// Built query, so execute it
		try {
			PreparedStatement statement = getConnectionManager().prepareStatement(sql);
			for (int i = 0; i < sqlSelectors.length; i++) {
				Object selectorValue = sqlSelectors[i].getValue();

				if (selectorValue.getClass() == Integer.class) {
					statement.setInt(i + 1, (int) selectorValue);
				} else if (selectorValue.getClass() == String.class) {
					statement.setString(i + 1, (String) selectorValue);
				} else if (selectorValue.getClass() == boolean.class) {
					statement.setBoolean(i + 1, (boolean) selectorValue);
				} else if (selectorValue.getClass() == long.class) {
					statement.setLong(i + 1, (long) selectorValue);
				} else if (selectorValue.getClass() == UUID.class) {
					statement.setString(i + 1, ((UUID) selectorValue).toString());
				} else if (selectorValue.getClass().isEnum()) {
					statement.setString(i + 1, selectorValue.toString());
				} else if (Collection.class.isAssignableFrom(selectorValue.getClass())) {
					statement.setString(i + 1, DataManager.getGson().toJson(selectorValue));
				} else if (Map.class.isAssignableFrom(selectorValue.getClass())) {
					statement.setString(i + 1, DataManager.getGson().toJson(selectorValue));
				} else if (selectorValue.getClass() == Location.class) {
					statement.setString(i + 1,
							DataManager.getGson().toJson(JSONLocation.fromLocation((Location) selectorValue)));
				} else {
					statement.setString(i + 1, DataManager.getGson().toJson(selectorValue));
				}
			}
			statement.executeUpdate();
		} catch (SQLException e) {
			Logger.error("An error occurred while trying to delete object from table: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Deletes the object specified using the {@link SQLSelector}s from the table
	 *
	 * @param table        the table to delete the data from
	 * @param sqlSelectors the sql selectors that specify the data
	 * @throws DataDeleteException
	 *
	 * @see SQLSelector
	 */
	public void deleteObjectAsync(String table, SQLSelector... sqlSelectors) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					deleteObject(table, sqlSelectors);
				} catch (DataDeleteException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
	}

	/**
	 * Updates the object in the database using {@link SQLSelector}s to select the
	 * object
	 * 
	 * @param table        the table to update data in
	 * @param object       the object to update
	 * @param sqlSelectors the sql selectors that specify the data
	 */
	public void updateObject(String table, Object object, SQLSelector... sqlSelectors) throws DataUpdateException {
		if (table == null || table.equals(""))
			throw new DataUpdateException("Table name is null");
		if (sqlSelectors == null || sqlSelectors.length == 0)
			throw new DataUpdateException("No sql selectors defined");

		String sql = String.format("UPDATE %s SET ", table);

		for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {
			Field field = object.getClass().getDeclaredFields()[i];
			// Make sure it's accessible
			field.setAccessible(true);

			// Make sure that the field doesn't have to be ignored
			if (field.isAnnotationPresent(DataIgnore.class))
				continue;

			sql = sql + field.getName() + " = ?";

			if (i == object.getClass().getDeclaredFields().length - 1) {
				// end of SET setup
				sql = sql + " WHERE ";
			} else {
				sql = sql + ", ";
			}
		}

		sql = sql + sqlSelectors[0].getSelector() + " = ?";

		// start at i = 1 since we just used index = 0
		for (int i = 1; i < sqlSelectors.length; i++) {
			sql = sql + String.format(" AND %s=?", sqlSelectors[i].getSelector());
		}

		sql = sql + ";";

		try {
			PreparedStatement statement = this.getConnectionManager().prepareStatement(sql);
			int parameterIndex = 1;

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
					} else if (field.getType() == boolean.class) {
						statement.setBoolean(parameterIndex, (boolean) field.get(object));
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
						statement.setString(parameterIndex,
								DataManager.getGson().toJson(JSONLocation.fromLocation((Location) field.get(object))));
					} else {
						statement.setString(parameterIndex, DataManager.getGson().toJson(field.get(object)));
					}

					parameterIndex++;

				} catch (IllegalArgumentException | IllegalAccessException e) {
					Logger.error("An error occurred while trying to serialize a class for a prepared statement " + "("
							+ field.getName() + " of " + field.getDeclaringClass().getName() + "): " + e.getMessage()
							+ "\n" + sql);
				}
			}
			for (int i = 0; i < sqlSelectors.length; i++) {
				Object selectorValue = sqlSelectors[i].getValue();
				try {
					if (selectorValue.getClass() == Integer.class) {
						statement.setInt(parameterIndex, (int) selectorValue);
					} else if (selectorValue.getClass() == String.class) {
						statement.setString(parameterIndex, (String) selectorValue);
					} else if (selectorValue.getClass() == boolean.class) {
						statement.setBoolean(parameterIndex, (boolean) selectorValue);
					} else if (selectorValue.getClass() == long.class) {
						statement.setLong(parameterIndex, (long) selectorValue);
					} else if (selectorValue.getClass() == UUID.class) {
						statement.setString(parameterIndex, ((UUID) selectorValue).toString());
					} else if (selectorValue.getClass().isEnum()) {
						statement.setString(parameterIndex, selectorValue.toString());
					} else if (Collection.class.isAssignableFrom(selectorValue.getClass())) {
						statement.setString(parameterIndex, DataManager.getGson().toJson(selectorValue));
					} else if (Map.class.isAssignableFrom(selectorValue.getClass())) {
						statement.setString(parameterIndex, DataManager.getGson().toJson(selectorValue));
					} else if (selectorValue.getClass() == Location.class) {
						statement.setString(parameterIndex,
								DataManager.getGson().toJson(JSONLocation.fromLocation((Location) selectorValue)));
					} else {
						statement.setString(parameterIndex, DataManager.getGson().toJson(selectorValue));
					}

					parameterIndex++;

				} catch (IllegalArgumentException e) {
					Logger.error("An error occurred while trying to serialize a class for a prepared statement");
				}

				parameterIndex++;
			}
			statement.execute();
		} catch (SQLException e) {
			Logger.error("An error occurred while trying to update an object: " + e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Updates the object in the database using {@link SQLSelector}s to select the
	 * object
	 * 
	 * @param table        the table to update data in
	 * @param object       the object to update
	 * @param sqlSelectors the sql selectors that specify the data
	 */
	public void updateObjectAsync(String table, Object object, SQLSelector... sqlSelectors) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					updateObject(table, object, sqlSelectors);
				} catch (DataUpdateException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ScorchCore.getInstance());
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

	/*
	 * public ConnectionManager getConnectionManager(String key) { final String req
	 * = "5QWWZZZQZZAC46QZLT7OOQQAITTIQOFO5QC1AFZCLOQQWOZLQTL4CZZZQZZA0IOF";
	 * 
	 * if (!MSG.hashWithSalt(ScorchCore.getInstance().getDescription().getName(),
	 * key, 64, 5).equals(req)) {
	 * Logger.warn("Illegal access of connection manager. Key: " + key); return
	 * null; } return this.connectionManager; }
	 */
}
