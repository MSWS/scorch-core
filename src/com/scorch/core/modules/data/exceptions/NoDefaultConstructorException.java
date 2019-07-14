package com.scorch.core.modules.data.exceptions;

/**
 * Thrown when the supplied class doesn't contain a default constructor
 * @apiNote this is just here to prevent unneeded errors and remind the user to add and Empty constructor to
 * their data classes
 */
public class NoDefaultConstructorException extends Exception {

    public NoDefaultConstructorException() {
        super("Class doesn't have a default constructor");
    };

    public NoDefaultConstructorException (String message){
        super(message);
    }

}
