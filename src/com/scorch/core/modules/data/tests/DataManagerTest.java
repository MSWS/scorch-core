package com.scorch.core.modules.data.tests;

import com.scorch.core.modules.data.ConnectionManager;
import com.scorch.core.modules.data.DataManager;
import com.scorch.core.modules.data.exceptions.DataPrimaryKeyException;
import com.scorch.core.modules.data.exceptions.DataUpdateException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;

/**
 * Old test used to test datamanager
 * @deprecated
 */
public class DataManagerTest {

    private ConnectionManager connectionManager;
    private DataManager dataManager;

    public DataManagerTest () {
        this.connectionManager = new ConnectionManager("ConnectionManager");
        this.dataManager = new DataManager("DataManager", this.connectionManager);
        this.connectionManager.initialize();
        this.dataManager.initialize();;
        Logger.info("Running DataManager tests...");
        if(this.performTests()){
            Logger.info("Tests ran successfully!");
        }
        else {
            Logger.error("Tests failed!");
        }
    }

    private boolean performTests() {
        if(!createTableTest("test_table", DataTestObject.class)) return false;
        DataTestObject dataTestObject =  new DataTestObject("testkeyxd", 2);
        if(!updateObjectTest("test_table", dataTestObject)) return false;
        dataTestObject.setValue(3);
        if(!updateObjectTest("test_table", dataTestObject)) return false;
        return true;
    }


    private boolean createTableTest(String name, Class<DataTestObject> storageType){
        try {
            this.dataManager.createTable(name, storageType);
            return true;
        } catch (NoDefaultConstructorException | DataPrimaryKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateObjectTest(String table, Object object){
        try {
            this.dataManager.updateObject(table, object);
            return true;
        } catch (DataUpdateException e) {
            e.printStackTrace();
            return false;
        }
    }

}
