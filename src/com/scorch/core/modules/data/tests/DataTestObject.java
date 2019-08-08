package com.scorch.core.modules.data.tests;

import com.scorch.core.modules.data.annotations.DataPrimaryKey;

/**
 * Made for {@link DataManagerTest}
 * @deprecated
 */
public class DataTestObject {

    @DataPrimaryKey
    private String uniqueKey;

    private int value = 0;

    public DataTestObject () {

    }

    public DataTestObject (String uniqueKey, int value) {
        this.uniqueKey = uniqueKey;
        this.value = value;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
