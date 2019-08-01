package com.scorch.core.modules.communication;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.scorch.core.modules.data.annotations.DataIgnore;

/**
 * Custom exclusion strategy for {@link com.google.gson.Gson}.
 * This strategy will ignore any fields with the {@link DataIgnore} annotation
 */
public class ExcludeStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotations().contains(DataIgnore.class);
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
