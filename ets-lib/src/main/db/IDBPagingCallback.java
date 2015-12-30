package com.etrans.lib.db;

/**
 * Created by maotz on 2015-04-25.
 * 查询结果反馈回调接口
 */
public interface IDBPagingCallback {
    void onResult(DBPagingResult _result);
}
