package com.scorch.core.modules.data.exceptions;

/**
 * Thrown when something goes wrong while trying to get data from the database in {@link com.scorch.core.modules.data.DataManager}
 * @apiNote I feel like this might be a bit redundant since we can just check for nulls, but it is the proper way to do it
 */
public class DataObtainException extends Exception {
    /**
     * Creates a new {@link com.scorch.core.modules.data.exceptions.DataObtainException}
     * @param message the reason why the exception is thrown
     */
    public DataObtainException (String message){
        super(message);
    }
}
