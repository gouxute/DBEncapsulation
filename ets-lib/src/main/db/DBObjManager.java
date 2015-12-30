package com.etrans.lib.db;

import com.etrans.lib.kryo.KryoObjManager;

/**
 * Created by maotz on 2015-04-03.
 * Connection management
 */
public class DBObjManager extends KryoObjManager{
    public DBObjManager(){
        super();
        registerClass(DBRequest.class);
        registerClass(DBResult.class);
    }
}
