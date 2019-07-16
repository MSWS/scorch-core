package com.scorch.core.modules.data;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.utils.Logger;

import java.sql.*;

/**
 *  ConnectionManager that handles SQL connections to the database heavily used by {@link DataManager}
 *
 * @see DataManager;
 * @author Gijs de Jong
 */
public class ConnectionManager extends AbstractModule {

	
	private String driver, host, user, password, database, port;
	private Connection connection;
	
	public ConnectionManager(String id) {
		super(id);
	}

	
	
	@Override
	public void initialize() {
		this.driver = "org.mariadb.jdbc.Driver";
		this.database = ScorchCore.getInstance().getConfig().getString("MySql.database");
		this.port = ScorchCore.getInstance().getConfig().getString("MySql.port");
		this.host = "jdbc:mariadb://" + ScorchCore.getInstance().getConfig().getString("MySql.host") + ":" + this.port + "/" + this.database;
		this.user = ScorchCore.getInstance().getConfig().getString("MySql.user");
		this.password = ScorchCore.getInstance().getConfig().getString("MySql.password");
		
		this.connect();
	}

	@Override
	public void disable() {
		if(this.isConnected()){
			try {
				this.getConnection().close();
			} catch (SQLException e) {
				Logger.error("Error while trying to close connection:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Executes <code>query</code> in sync
	 * @param  query the query to execute
	 * @return The {@link java.sql.ResultSet} returned by executing <code>query</code>
	 */
	public ResultSet executeQuery (String query) {
		if(this.isConnected()) {
			try {
				Statement statement = this.getConnection().createStatement();
				return statement.executeQuery(query);
			} catch (SQLException e) {
				Logger.error("Error occured while trying execute query: " + e.getMessage() + "\n" + query);
				return null;
			}
		}
		else {
			Logger.error("Couldn't execute query because there's no active connection to the database!\n" + query);
			return null;
		}
	}
	
	/**
	 * Creates a prepared statement with <code>query</code> as sql
	 * @param  query the sql to use in the statement
	 * @return A prepared statement instance that you can use to execute database actions
	 */
	public PreparedStatement prepareStatement (String query) {
		if(this.isConnected()) {
			try {
				return this.getConnection().prepareStatement(query);
			}
			catch(SQLException e) {
				Logger.error("Couldn't create prepared statement: " + e.getMessage() + "\n" + query);
				return null;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * Connects to the database
	 */
	private void connect () {
		try {
			Class.forName(this.driver);
			this.connection = DriverManager.getConnection(this.host, this.user, this.password);
		} catch (ClassNotFoundException | SQLException e) {
			Logger.error("Error occurred while trying to connect to database: " + e.getMessage());
			this.connection = null;
		}
	}
	
	/**
	 * Returns whether the plugin is connected to the database
	 * @return if the plugin's connected to the database
	 */
	public boolean isConnected () {
		if(this.getConnection() != null) {
			try {
				return !this.getConnection().isClosed();
			} catch (SQLException e) {
				Logger.error("Error occurred while trying to check the sql connection status: " + e.getMessage());
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * Gets the current connection and if it's null reconnect!
	 * @return the current connection to the database
	 */
	private Connection getConnection () {
		try {
			if(this.connection != null && !this.connection.isClosed()) {
				return this.connection;
			}
			else {
				this.connect();
				return connection;
			}
		} catch (SQLException e) {
			Logger.error("Error occurred while trying to check the sql connection status: " + e.getMessage());
			return null;
		}
	}
}
