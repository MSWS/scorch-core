package com.scorch.core.modules.data;

/**
 * A class used to select results from the database
 * @author Gijs de Jong
 */
public class SQLSelector {

    private final String selector;
    private final String value;

    /**
     * Creates a SQL selector instance
     * @param selector the selector to use
     * @param value    the value for the selector
     */
    public SQLSelector (String selector, String value){
        this.selector = selector;
        this.value = value;
    }

    /**
     * Gets the selector
     * @return the selector
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Gets the value
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
