package com.scorch.core.modules.communication.websocket.packets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class PacketUtils {

    private static Gson gson = new Gson();

    public static boolean isValidJSON (String value){
        try {
            return (gson.fromJson(value, JsonObject.class) != null);
        }
        catch (Exception e){
            return false;
        }
    }

    public static boolean isValidPacket (String value){
        if(!isValidJSON(value)) return false;
        JsonObject obj = gson.fromJson(value, JsonObject.class);
        return (obj.has("type") && obj.has("version") && obj.has("timestamp"));
    }

    public static String getPacketType (String value){
        if(!isValidJSON(value) && isValidPacket(value)) return null;
        return  gson.fromJson(value, JsonObject.class).get("type").getAsString();
    }

}
