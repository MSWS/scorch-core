package com.scorch.core.modules.communication;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.scorch.core.modules.data.annotations.DataIgnore;
import com.scorch.core.modules.data.wrappers.JSONPlayer;

public class NetworkEventSerializer implements JsonSerializer<NetworkEvent> {

    private Gson gson = new GsonBuilder().addSerializationExclusionStrategy(new ExcludeStrategy()).create();

    @Override
    public JsonElement serialize(NetworkEvent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        obj.addProperty("eventClassType",  src.getClass().getName());

        for(Field field : src.getClass().getDeclaredFields()){
            if(!field.isAnnotationPresent(DataIgnore.class)){
                try {
                    field.setAccessible(true);
                    if(field.getType() == Player.class){
                        obj.add(field.getName(), gson.toJsonTree(JSONPlayer.fromPlayer((Player)field.get(src))));
                    }
                    else {
                        obj.add(field.getName(), gson.toJsonTree(field.get(src)));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
}
