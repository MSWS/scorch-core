package com.scorch.core.modules.data.exceptions;

/**
 * Thrown when something goes wrong while updating the data in the database
 * @apiNote I feel like this might be a bit redundant since we can just check for nulls, but it is the proper way to do it
 */
public class DataUpdateException extends Exception {

    public DataUpdateException () {
        super("Error while updating data");
    }

    public DataUpdateException (String message){
        super(message);
    }

}
