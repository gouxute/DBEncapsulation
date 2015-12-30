package com.etrans.lib.db;

/**
 * Created by maotz on 2015-04-25.
 * 支持分页查询的接口
 */
public interface IDBPagingQuery {
    /**
     * 设置本地缓存文件
     * @param _db3_file 缓存文件路径
     * @throws Exception
     */
    void setup(String _db3_file) throws Exception;

    /**
     * 设置本地缓存文件
     * @param _db3_file 缓存文件路径
     * @param _db_config 数据访问参数
     * @throws Exception
     */
    void setup(String _db3_file, DBConfig _db_config) throws Exception;

    /**
     * 请求 异步查询
     * @param _config 数据库配置
     * @param _sql 语句
     * @param _callback 回调
     * @param _page_size 每页行数
     * @param _page_index 当前页号
     */
    void query(DBConfig _config, String _sql, int _page_size, int _page_index, IDBPagingCallback _callback) throws Exception;
    void query(String _sql, int _page_size, int _page_index, IDBPagingCallback _callback) throws Exception;

    /**
     * 查询 同步执行
     * @param _config 数据库配置
     * @param _sql 语句
     * @param _page_size 每页行数
     * @param _page_index 当前页号
     */
    DBPagingResult query(DBConfig _config, String _sql, int _page_size, int _page_index) throws Exception;
    DBPagingResult query(String _sql, int _page_size, int _page_index) throws Exception;
}
