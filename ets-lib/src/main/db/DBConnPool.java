package com.etrans.lib.db;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by maotz on 2015-03-27.
 * Database connection pool;
 * Temporarily save useless connecting object
 * The object not used for a long time will be closed
 */
public class DBConnPool {
    /**
     * 日志记录对象
     */
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DBConnPool.class);

    /**
     * 每个关键字上，允许的空闲链接数
     */
    private final int allowFreeCount;

    /**
     * 每个空闲链接数，允许存在的豪秒数
     */
    private final int allowFreeMsec;

    /**
     * 空闲对象哈希表
     */
    private final Map<String, List<DBConn>> consHashByKey;

    /**
     * 每个链接最少存活这么多秒
     */
    private final static int MIN_IDEL_SECS = 30;
    /**
     * 每个链接最多存活这么多秒
     */
    private final static int MAX_IDEL_SECS = 600;

    /**
     * 相同配置下，可存储多少个链接
     */
    private final static int MAX_CONN_COUNT = 5;

    /**
     * 构造函数
     * @param _allow_free_count 每个关键字上，允许的空闲链接数
     * @param _allow_idel_secs 每个空闲链接数，允许存在的秒数
     */
    public DBConnPool(int _allow_free_count, int _allow_idel_secs){

        if(_allow_free_count>MAX_CONN_COUNT)
            _allow_free_count = MAX_CONN_COUNT;
        else if(_allow_free_count<1)
            _allow_free_count = 1;
        allowFreeCount  = _allow_free_count;

        if(_allow_idel_secs < MIN_IDEL_SECS)
            _allow_idel_secs = MIN_IDEL_SECS;
        else if(_allow_idel_secs > MAX_IDEL_SECS)
            _allow_idel_secs = MAX_IDEL_SECS;
        allowFreeMsec   = _allow_idel_secs * 1000;

        consHashByKey   = new ConcurrentHashMap<>();

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        // 每 5秒钟检查一次所有链接，检测是否有过期的链接
        executor.schedule(new Runnable() {
            public void run() {
            checkIdel();
            }
        }, 5, TimeUnit.SECONDS);
    }

    /**
     * 定期检查是否有链接空闲
     */
    synchronized private void checkIdel(){
        long now = System.currentTimeMillis();
        for(List<DBConn> list : consHashByKey.values()){
            for(DBConn dbConn : list){
                if(now - dbConn.activeTime < allowFreeMsec)
                    continue;
                list.remove(dbConn);
                dbConn.close();
            }
        }
    }

    /**
     * 放入一个连接对象
     * @param _db_conn 链接对象
     */
    synchronized public void put(DBConn _db_conn){
        if(null!=_db_conn && !_db_conn.isClosed()) {
            String key = _db_conn.config.getKey();
            List<DBConn> list = consHashByKey.get(key);

            if(null==list) {
                list = new LinkedList<>();
                consHashByKey.put(key, list);
            }

            if(list.size()<allowFreeCount)
                list.add(_db_conn);
            else
                _db_conn.close();
        }
    }

    /**
     * 根据配置获取数据库连接
     * @param _config 配置信息
     * @return 链接
     */
    synchronized public DBConn get(DBConfig _config){
        DBConn db_conn = null;
        if(null!=_config) {
            List<DBConn> list = consHashByKey.get(_config.getKey());
            if(null!=list && !list.isEmpty()){
                db_conn = list.remove(0);
            }
        }
        return db_conn;
    }

}
