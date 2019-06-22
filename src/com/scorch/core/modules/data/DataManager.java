package com.scorch.core.modules.data;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import com.google.gson.Gson;
import com.scorch.annotations.DataIgnore;
import com.scorch.annotations.DataNotNull;
import com.scorch.core.modules.AbstractModule;

public class DataManager extends AbstractModule{

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
	 * Creates a table with <code>name</code> as name and with the 
	 * variables of <code>storageType</code> as columns
	 * @param name        the name of the database
	 * @param storageType the column template
	 */
	public void createTable (String name, Object storageType) {
		
		String query = "CREATE TABLE IF NOT EXISTS " + name + " (table_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, ";
		
		for(int i = 0; i < storageType.getClass().getFields().length; i++) {
			
			Field field = storageType.getClass().getFields()[i];
			
			//Makes sure that the field doesnt have to be ignored for serialisation
			if(field.getAnnotation(DataIgnore.class) == null) {
				
				query = query + field.getName();
				
				if(field.getType() == Integer.class) {
					query = query + " INT ";
				}
				else if(field.getType() == String.class) {
					query = query + " TEXT ";
				}
				else if(field.getType() == Long.class){
					query = query + " BIGINT ";
				}
				else if(field.getType() == UUID.class) {
					query = query + " VARCHAR(36) ";
				}
				else if(field.getType().isEnum()) {
					query = query + " TEXT ";
				}
				else if(Collection.class.isAssignableFrom(field.getClass())) {
					query = query + " TEXT ";
				}
				else if(Map.class.isAssignableFrom(field.getClass())) {
					query = query + " TEXT ";
				}
				else if(field.getType() == Location.class) {
					query = query + " TEXT ";
				}
				else {
					query = query + " TEXT ";
				}
				
				if(field.getAnnotation(DataNotNull.class) != null) {
					query = query + "NOT NULL, ";
				}
				else if(i != storageType.getClass().getFields().length) {
					query = query + ", ";
				}
				else {
					query = query + ");";
				}
				
			}
		}
	}
	
	
	
	/**
	 * Gets the gson instance
	 * @return the gson instance
	 */
	public static Gson getGson () {
		return gson;
	}
	
	/**
	 * Gets the connectionmanager
	 * @return the connection manager
	 */
	private ConnectionManager getConnectionManager () {
		return this.connectionManager;
	}
}
