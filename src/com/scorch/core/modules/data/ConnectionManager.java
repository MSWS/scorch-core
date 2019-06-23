package com.scorch.core.modules.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.utils.Logger;

public class ConnectionManager extends AbstractModule {

	private String driver, host, user, password, database, port;
	private Connection connection;

	public ConnectionManager(String id) {
		super(id);
		// Setup configuration file
		ScorchCore.getInstance().getConfig().options().copyDefaults(true);
		ScorchCore.getInstance().saveConfig();
	}

	@Override
	public void initialize() {
		this.driver = "org.mariadb.jdbc.Driver";
		this.database = ScorchCore.getInstance().getConfig().getString("MySql.database");
		this.port = ScorchCore.getInstance().getConfig().getString("MySql.port");
		this.host = "jdbc:mariadb://" + ScorchCore.getInstance().getConfig().getString("MySql.host") + ":" + this.port
				+ "/" + this.database;
		this.user = ScorchCore.getInstance().getConfig().getString("MySql.user");
		this.password = ScorchCore.getInstance().getConfig().getString("MySql.password");

		this.connect();

		// TODO setup base tables here

	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub
		closeConnection();
	}
<<<<<<< HEAD

=======
	
	/**
	 * Executes <code>query</code> in sync
	 * @param  query the query to execute
	 * @return The <code>ResultSet</code> returned by executing <code>query</code>
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
	
>>>>>>> 6888e607ca65522422a0abe1719f2666d7a04799
	/**
	 * Connects to the database
	 */
	private synchronized void connect() {
		try {
			Class.forName(this.driver);
			this.connection = DriverManager.getConnection(this.host, this.user, this.password);
		} catch (ClassNotFoundException | SQLException e) {
			Logger.error("Error occured while trying to connect to database: " + e.getMessage());
			this.connection = null;
		}
	}

	private synchronized void closeConnection() {
		if (!isConnected())
			return;
		try {
			connection.close();
		} catch (SQLException e) {
			Logger.error("Error occured while closing connection: " + e.getMessage());
		}
	}

	/**
	 * Returns whether the plugin is connected to the database
	 * 
	 * @return if the plugin's connected to the database
	 */
	public boolean isConnected() {
		if (this.getConnection() != null) {
			try {
				return !this.getConnection().isClosed();
			} catch (SQLException e) {
				Logger.error("Error occured while trying to check the sql connection status: " + e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Gets the current connection and if it's null reconnect!
	 * 
	 * @return the current connection to the database
	 */
	private Connection getConnection() {
		try {
			if (this.connection != null && !this.connection.isClosed()) {
				return this.connection;
			} else {
				this.connect();
				return connection;
			}
		} catch (SQLException e) {
			Logger.error("Error occured while trying to check the sql connection status: " + e.getMessage());
			return null;
		}
	}
}
