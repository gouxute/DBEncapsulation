package com.etrans.lib.db;

/**
 * Created by maotz on 2015-03-27.
 * 数据操作结果监听器接口
 */
public interface IDBResultListener {
    /**
     * 收到过期事件
     */
    void onExpired(DBRequest _request);
    /**
     * 接收到结果事件
     * @param _result 结果
     */
    void onResult(DBResult _result);
}
