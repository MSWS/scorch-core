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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.modules.data.annotations.DataNotNull;
import com.scorch.core.modules.data.annotations.DataPrimaryKey;
import com.scorch.core.modules.data.exceptions.DataDeleteException;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.DataPrimaryKeyException;
import com.scorch.core.modules.data.exceptions.DataUpdateException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.modules.data.wrappers.JSONLocation;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.MSG;

/*
 * Utility to easily save different types of objects to a database and load them
 *
 * @author Gijs de Jong
 */
public class DataManager extends AbstractModule {

	// Maybe change the gson configuration sometime in the future.
	private static Gson gson = new GsonBuilder().create();

	private ConnectionManager connectionManager;
	private Map<UUID, ScorchPlayer> cache;

	public DataManager(String id, ConnectionManager connectionManager) {
		super(id);
		this.connectionManager = connectionManager;
	}

	@Override
	public void initialize() {
		cache = new HashMap<>();

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					createTable("players", ScorchPlayer.class);
				} catch (NoDefaultConstructorException | DataPrimaryKeyException e1) {
					e1.printStackTrace();
				}
			}
		}.runTaskLater(ScorchCore.getInstance(), 20); // Make sure connection to database has been established

		new BukkitRunnable() {
			@Override
			public void run() {
				savePlayerData();
			}
		}.runTaskTimerAsynchronously(ScorchCore.getInstance(), 6000, 6000); // 5 Minutes
	}

	@Override
	public void disable() {
		for (Entry<UUID, ScorchPlayer> entry : cache.entrySet()) {
			try {
				updateObject("players", entry.getValue());
			} catch (DataUpdateException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates a table if it doesn't exist already with <code>name</code> as name
	 * and with the fields of <code>storageType</code> as columns Call this in your
	 * plugin/module initialisation
	 *
	 * @param name        the name of the database
	 * @param storageType the column template
	 */
	public void createTable(String name, Class<?> storageType)
			throws NoDefaultConstructorException, DataPrimaryKeyException {

		if (!hasDefaultConstructor(storageType)) {
			throw new NoDefaultConstructorException();
		}

		if (!hasValidPrimaryKey(storageType)) {
			throw new DataPrimaryKeyException(
					storageType.getName() + " Doesn't have a valid primary key value! (none at all or multiple!)");
		}

		String query = "CREATE TABLE IF NOT EXISTS " + name + " (object_type TEXT NOT NULL, ";

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

			if (field.getType() == int.class) {
				query += " INT";
			} else if (field.getType() == String.class) {
				if (field.isAnnotationPresent(DataPrimaryKey.class)) {
					query += " VARCHAR(255)";
				} else {
					query += " TEXT";
				}
			} else if (field.getType() == boolean.class) {
				query += " BOOLEAN ";
			} else if (field.getType() == long.class) {
				query += " BIGINT";
			} else if (field.getType() == UUID.class) {
				query += " VARCHAR(36)";
			} else if (field.getType().isEnum()) {
				if (field.isAnnotationPresent(DataPrimaryKey.class)) {
					query += " VARCHAR(255)";
				} else {
					query += " TEXT";
				}
			} else if (Collection.class.isAssignableFrom(field.getType())) {
				if (field.isAnnotationPresent(DataPrimaryKey.class)) {
					query += " VARCHAR(255)";
				} else {
					query += " TEXT";
				}
			} else if (Map.class.isAssignableFrom(field.getType())) {
				if (field.isAnnotationPresent(DataPrimaryKey.class)) {
					query += " VARCHAR(255)";
				} else {
					query += " TEXT";
				}
			} else if (field.getType() == Location.class) {
				if (field.isAnnotationPresent(DataPrimaryKey.class)) {
					query += " VARCHAR(255)";
				} else {
					query += " TEXT";
				}
			} else {
				if (field.isAnnotationPresent(DataPrimaryKey.class)) {
					query += " VARCHAR(255)";
				} else {
					query += " TEXT";
				}
			}

			if (field.isAnnotationPresent(DataNotNull.class)) {
				query += " NOT NULL ";
			}

			if (field.isAnnotationPresent(DataPrimaryKey.class)) {
				query += " PRIMARY KEY";
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
	 * @deprecated You should always use
	 *             {@link DataManager#updateObject(String, Object)} in the future
	 *             this method will be removed and
	 *             {@link DataManager#updateObject(String, Object)} will be renamed
	 *             to "saveObject"
	 *
	 *             Saves the object to table, automatically parses all fields of the
	 *             object to their sql equivalent. <br>
	 *             Supported field types include:</b> <br>
	 *             <ul>
	 *             <li>{@link Integer}</li>
	 *             <li>{@link String}</li>
	 *             <li>{@link Boolean}</li>
	 *             <li>{@link Long}</li>
	 *             <li>{@link UUID}</li>
	 *             <li>{@link Location}</li>
	 *             <li>{@link Collection}</li>
	 *             <li>{@link Map}</li>
	 *             </ul>
	 *             </br>
	 *             <br>
	 *             If the type isn't listed above, it will try to convert the object
	 *             to json using Google's {@link Gson}</br>
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
			if (field.isAnnotationPresent(DataIgnore.class)) {
				continue;
			}

			query += field.getName();

			// if the next field had a DataIgnore class then this code would result in an
			// unfinished statement "(?,?,?" for example

			if (i == object.getClass().getDeclaredFields().length - 1
					|| object.getClass().getDeclaredFields()[i + 1].isAnnotationPresent(DataIgnore.class)) {
				query += ") VALUES (?,";
				for (int j = 0; j < object.getClass().getDeclaredFields().length; j++) {
					if (object.getClass().getDeclaredFields()[j].isAnnotationPresent(DataIgnore.class))
						continue;
					query += "?";
					if (j == object.getClass().getDeclaredFields().length - 1
							|| object.getClass().getDeclaredFields()[j + 1].isAnnotationPresent(DataIgnore.class)) {
						query += ");";
					} else {
						query += ", ";
					}
				}
			} else {
				query += ", ";
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
					if (field.getType() == int.class) {
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
						statement.setString(parameterIndex, field.get(object) + "");
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

				// Start at 2 since columns start counting at 1 and we need to skip the first
				// (object_type)
				int columnIndex = 2;

				// Start at one since we've already gotten the class name ^
				for (Field field : clazz.getDeclaredFields()) {

					// Make sure the field doesn't have to be ignored
					if (field.isAnnotationPresent(DataIgnore.class))
						continue;

					// Make sure the field is accessible in case it's private
					field.setAccessible(true);

					if (field.getType() == int.class) {
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
			if (collection.size() > 1)
				return collection;
			else
				return collection.stream().findFirst().orElse(null);
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

				// Start at 2 since columns start counting at 1 and we need to skip the first
				// (object_type)
				int columnIndex = 2;

				// Start at one since we've already gotten the class name ^
				for (Field field : clazz.getDeclaredFields()) {

					// Make sure the field doesn't have to be ignored
					if (field.isAnnotationPresent(DataIgnore.class))
						continue;

					// Make sure the field is accessible in case it's private
					field.setAccessible(true);

					if (field.getType() == int.class) {
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
						try {
							field.set(dataObject, field.getType().getMethod("valueOf", String.class).invoke(null,
									res.getString(columnIndex)));
						} catch (InvocationTargetException e) {
							field.set(dataObject, null);
						}
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
				| ClassNotFoundException e) {
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

				if (selectorValue.getClass() == int.class) {
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
	 * @param table  the table to update data in
	 * @param object the object to update
	 */
	public void updateObject(String table, Object object) throws DataUpdateException {
		if (table == null || table.equals(""))
			throw new DataUpdateException("Table name is null");

		String sql = String.format("INSERT INTO %s (object_type", table);

		for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {
			Field field = object.getClass().getDeclaredFields()[i];
			// Ignore field if the annotation is present
			if (field.isAnnotationPresent(DataIgnore.class)) {
				if (i == object.getClass().getDeclaredFields().length - 1) {
					sql += ") VALUES (";
				}
				continue;
			}

			if (i == object.getClass().getDeclaredFields().length - 1) {
				sql += ", " + field.getName() + ") VALUES (?, ";
			} else {
				sql += ", " + field.getName();
			}
		}

		for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {

			// Ignore field if the annotation is present
			if (object.getClass().getDeclaredFields()[i].isAnnotationPresent(DataIgnore.class)) {
				if (i == object.getClass().getDeclaredFields().length - 1) {
					sql += "?) ON DUPLICATE KEY UPDATE ";
				}
				continue;
			}

			if (i == object.getClass().getDeclaredFields().length - 1) {
				sql += "?) ON DUPLICATE KEY UPDATE ";
			} else {
				sql += "?, ";
			}
		}

		for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {
			Field field = object.getClass().getDeclaredFields()[i];
			// Ignore field if annotation is present
			if (field.isAnnotationPresent(DataIgnore.class)) {
				if (i == object.getClass().getDeclaredFields().length - 1) {
					sql += ";";
				} else if (i == 0) {
					Field tmp = field;
					while (i < object.getClass().getDeclaredFields().length) {
						tmp = object.getClass().getDeclaredFields()[i];
						if (tmp.isAnnotationPresent(DataIgnore.class)) {
							i++;
							continue;
						}

						sql += tmp.getName() + "=VALUES(" + tmp.getName() + ")";

						if (i == object.getClass().getDeclaredFields().length - 1)
							sql += ";";
						break;
					}
				}
				continue;
			}

			if (i == object.getClass().getDeclaredFields().length - 1) {
				sql += ", " + field.getName() + "=VALUES(" + field.getName() + ");";
			} else if (i == 0) {
				sql += field.getName() + "=VALUES(" + field.getName() + ")";
			} else {
				sql += ", " + field.getName() + "=VALUES(" + field.getName() + ")";
			}
		}

		try {
			PreparedStatement statement = getConnectionManager().prepareStatement(sql);

			// column indices start at 1 and we need to skip the first one so 2
			int columnIndex = 2;

			statement.setString(1, object.getClass().getName());

			for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {
				// Ignore field if annotation is present
				if (object.getClass().getDeclaredFields()[i].isAnnotationPresent(DataIgnore.class))
					continue;
				Field field = object.getClass().getDeclaredFields()[i];

				field.setAccessible(true);

				if (field.getType() == int.class) {
					statement.setInt(columnIndex, (int) field.get(object));
				} else if (field.getType() == String.class) {
					statement.setString(columnIndex, (String) field.get(object));
				} else if (field.getType() == boolean.class) {
					statement.setBoolean(columnIndex, (boolean) field.get(object));
				} else if (field.getType() == long.class) {
					statement.setLong(columnIndex, (long) field.get(object));
				} else if (field.getType() == UUID.class) {
					statement.setString(columnIndex, ((UUID) field.get(object)).toString());
				} else if (field.getType().isEnum()) {
					statement.setString(columnIndex, field.get(object)+"");
				} else if (Collection.class.isAssignableFrom(field.getType())) {
					statement.setString(columnIndex, DataManager.getGson().toJson(field.get(object)));
				} else if (Map.class.isAssignableFrom(field.getType())) {
					statement.setString(columnIndex, DataManager.getGson().toJson(field.get(object)));
				} else if (field.getType() == Location.class) {
					statement.setString(columnIndex,
							DataManager.getGson().toJson(JSONLocation.fromLocation((Location) field.get(object))));
				} else {
					statement.setString(columnIndex, DataManager.getGson().toJson(field.get(object)));
				}
				columnIndex++;
			}

			statement.executeUpdate();
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the object in the database using {@link SQLSelector}s to select the
	 * object
	 *
	 * @param table  the table to update data in
	 * @param object the object to update
	 */
	public void updateObjectAsync(String table, Object object) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					updateObject(table, object);
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

	private boolean hasValidPrimaryKey(Class<?> type) {
		int valid = 0;
		for (Field field : type.getDeclaredFields()) {
			if (field.isAnnotationPresent(DataPrimaryKey.class)) {
				valid += 1;
			}
		}
		return (valid == 1);
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

	public ScorchPlayer getScorchPlayer(UUID uuid) {
		if (cache.containsKey(uuid))
			return cache.get(uuid);

		ScorchPlayer player = null;
		try {
			player = (ScorchPlayer) getObject("players", new SQLSelector("uuid", uuid.toString()));
			if (Bukkit.getPlayer(uuid) != null)
				MSG.tell(Bukkit.getPlayer(uuid), "&7Your data has successfully been loaded.");
		} catch (DataObtainException e) {
			e.printStackTrace();
		}

		if (player == null) {
			player = new ScorchPlayer(uuid, Bukkit.getOfflinePlayer(uuid).getName(), new HashMap<>());
			saveObject("players", player);
		}

		if (player.getName() == null) {
			player.setName(Bukkit.getOfflinePlayer(uuid).getName());
		}

		cache.put(uuid, player);
		return player;
	}

	public void savePlayerData() {
		try {
			Iterator<Entry<UUID, ScorchPlayer>> it = cache.entrySet().iterator();
			while (it.hasNext()) {
				Entry<UUID, ScorchPlayer> entry = it.next();
				Logger.log("Saving player data of %s", entry.getKey());
				updateObject("players", entry.getValue());
				if (Bukkit.getPlayer(entry.getKey()) == null) // Remove player data if the player is no longer
					it.remove(); // on the server
			}
		} catch (DataUpdateException e) {
			e.printStackTrace();
		}
	}

}
