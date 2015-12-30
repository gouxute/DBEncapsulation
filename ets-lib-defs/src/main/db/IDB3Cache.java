package com.etrans.lib.db;

import java.sql.ResultSet;

/**
 * Created by mous on 2015-04-11.
 * 数据缓存接口
 */
public interface IDB3Cache {
    /**
     * 判断表是否存在
     * @param _table 表名称
     * @return true: 有此表，false：无此表
     * @throws Exception
     */
    boolean hasTable(String _table) throws Exception;

    /**
     * 按表名称存储结果集
     * @param _table 表名称
     * @param _rs 结果集
     * @throws Exception
     */
    int put(String _table, ResultSet _rs) throws Exception;

    /**
     * 按语句获取结果集
     * @param _sql 语句
     * @return 结果集
     * @throws Exception
     */
    ResultSet get(String _sql) throws Exception;
}
