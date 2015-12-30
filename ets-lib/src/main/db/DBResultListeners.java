package com.etrans.lib.db;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by maotz on 2015-03-27.
 * 结果监听器集合
 */
public class DBResultListeners implements IDBResultListener{

    private final List<IDBResultListener> listeners = new LinkedList<>();

    public void registerListener(IDBResultListener _listener){
        if(null!=_listener && !listeners.contains(_listener))
            listeners.add(_listener);
    }

    public void onExpired(DBRequest _request){
        for(IDBResultListener listener : listeners)
            listener.onExpired(_request);
    }

    public void onResult(DBResult _result) {
        for(IDBResultListener listener : listeners)
            listener.onResult(_result);
    }
}
