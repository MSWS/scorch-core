package com.scorch.core.modules.communication.exceptions;

/**
 * An exception that's thrown when something goes wrong with the websocket connection!
 */
public class WebSocketException extends Exception {

    public WebSocketException (String message){
        super(message);
    }

    public WebSocketException (){
        this("An exception occured with the websocket!");
    }

}
