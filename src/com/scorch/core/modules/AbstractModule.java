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
	 * 
	 */
	public abstract void initialize();

	public abstract void disable();

	public boolean isEnabled() {
		return this.enabled;
	}
	
	public void setEnabled (boolean enabled) {
		this.enabled = enabled;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
