package com.scorch.core.modules;

public abstract class AbstractModule {
	
	private boolean enabled;
	private String id;
	
	/**
	 * Creates an abstract class with the id <code>id</code>
	 * @param id
	 */
	public AbstractModule (String id) {
		this.setId(id);
	}
	
	/**
	 * Initialises the module, initialise your module here.
	 */
	public abstract void initialize();

	/**
	 * Disables the module, save data and clean up here.
	 */
	public abstract void disable();

	/**
	 * Checks if the module is enabled
	 * @return whether the module is enabled or not
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
	
	
	/**
	 * Changes the module's enabled state to <code>enabled</code>
	 * @param enabled the new state of the module
	 */
	public void setEnabled (boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Gets the id of the module
	 * @return the id of the module
	 */
	public String getId() {
		return id;
	}

	
	/**
	 * Sets the id of the module
	 * @param id the id to change it to
	 */
	public void setId(String id) {
		this.id = id;
	}
}
