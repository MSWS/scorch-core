package com.scorch.core.modules.data.annotations;

import java.lang.annotation.*;
import com.scorch.core.modules.data.exceptions.DataPrimaryKeyException;

/**
 * An annotation used to declare the primary key for a class,
 * for example the uuid of a player if you're storing currency
 * @apiNote if this annotation isn't present in the class, a {@link DataPrimaryKeyException} will be thrown
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPrimaryKey {
}
