package com.etrans.lib.db;

import com.etrans.lib.utils.Coder;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by maotz on 2015-04-25.
 * Data search supporting page division;
 * 支持分页的数据查询器
 *  输入：
 *      1、查询语句 SQL
 *      2、分页参数
 *
 *  同步输出：
 *      全部查询结果
 *
 *  异步输出：
 *      分页逐步输出结果
 *
 */
public class DBPagingQuery implements IDBPagingQuery{
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DBPagingQuery.class);

    private final DB3Cache db3 = new DB3Cache();
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>(32);
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Map<String,Connection> connectionMap = new ConcurrentHashMap<>();
    private boolean init = false;
    private DBConfig dbConfig;

    public DBPagingQuery(){
    }

    private final Runnable worker = new Runnable() {
        @Override
        public void run() {
            while (true){
                Task task = null;
                try {
                    task = taskQueue.take();
                    DBPagingResult result = execTask(task);
                    task.callback.onResult(result);
                }catch (Exception e){
                    logger.error("error on execTask({})", task, e);
                }
            }
        }
    };

    /**
     * 缓存中数据多长时间内有效，默认 10 分钟
     */
    private final static long C_CACHE_MAX_AGE  = 10 * 60 * 1000;

    /**
     * 缓存库里面表相关信息
     */
    private class History{
        final String tableName;
        final String fieldNames;
        final int rowCount;
        long lastActiveTime = System.currentTimeMillis();
        History(String _table_name, String _field_names, int _row_count){
            logger.debug("create.History");
            tableName = _table_name;
            fieldNames = _field_names;
            rowCount = _row_count;
        }
        boolean isDirty(){
            return System.currentTimeMillis() - lastActiveTime > C_CACHE_MAX_AGE;
        }
    }
    private final Map<String, History> historyMap = new ConcurrentHashMap<>();

    /**
     * 设置本地缓存文件
     * @param _db3_file 缓存文件路径
     * @throws Exception
     */
    public void setup(String _db3_file) throws Exception{
        setup(_db3_file, null);
    }

    /**
     * 设置本地缓存文件
     * @param _db3_file 缓存文件路径
     * @param _db_config 数据访问参数
     * @throws Exception
     */
    public void setup(String _db3_file, DBConfig _db_config) throws Exception{
        if(!init) {
            logger.info("setup({})", _db3_file);
            executor.execute(worker);
            dbConfig = _db_config;
            db3.setup(_db3_file);
            db3.setup(_db3_file);
            loadHistory();
            init = true;
        }
    }

    /**
     * 从数据库加载已有缓存表相关信息
     */
    private void loadHistory() throws Exception{
        List<String> names = db3.getTableNames();
        for(String table : names){
            makeHistory(table);
        }
    }

    private History makeHistory(String _table) throws Exception{
        ResultSet rs = db3.get("select * from " + _table + " limit 1");
        String fields = getFieldNames(rs);

        rs = db3.get("select count(*) from "+ _table);
        int rows = rs.getInt(1);

        History history = new History(_table, fields, rows);
        historyMap.put(_table, history);
        return history;
    }

    private String getFieldNames(ResultSet _rs) throws Exception{
        ResultSetMetaData meta = _rs.getMetaData();
        int cols = meta.getColumnCount();
        String fields = "";
        for(int i=1; i<=cols; i++) {
            fields += meta.getColumnLabel(i);
            if(i<cols)
                fields+=",";
        }
        return fields;
    }

    private class Task{
        final String tableName;
        final DBConfig config;
        final String sql;
        final IDBPagingCallback callback;
        final int pageSize;
        final int pageIndex;

        Task(DBConfig _config, String _sql, IDBPagingCallback _callback, int _page_size, int _page_index){

            if(_page_size<0)
                _page_size = 0;

            if(_page_index<1)
                _page_index = 1;

            config = _config;
            sql = _sql;
            callback = _callback;
            pageSize = _page_size;
            pageIndex = _page_index;

            String temp = config.host + config.name + sql;
            tableName = "TAB_"+Coder.MD5(temp.getBytes());
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append(" cfg:").append(config.toString());
            sb.append(",sql:").append(sql);
            sb.append(",pageSize:").append(pageSize);
            sb.append(",pageIndex:").append(pageIndex);
            return sb.toString();
        }
    }

    /**
     * 请求 异步查询
     * @param _config 数据库配置
     * @param _sql 语句
     * @param _callback 回调
     * @param _page_size 每页行数
     * @param _page_index 当前页号
     */
    public void query(DBConfig _config, String _sql, int _page_size, int _page_index, IDBPagingCallback _callback) throws Exception{
        checkTimeout();
        Task task = new Task(_config, _sql, _callback, _page_size, _page_index);
        logger.debug("async query task={}", task);
        taskQueue.offer(task);
    }

    public void query(String _sql, int _page_size, int _page_index, IDBPagingCallback _callback) throws Exception{
        query(dbConfig, _sql, _page_size, _page_index, _callback);
    }

    /**
     * 查询 同步执行
     * @param _config 数据库配置
     * @param _sql 语句
     * @param _page_size 每页行数
     * @param _page_index 当前页号
     */
    public DBPagingResult query(DBConfig _config, String _sql, int _page_size, int _page_index) throws Exception{
        checkTimeout();
        Task task = new Task(_config, _sql, null, _page_size, _page_index);
        logger.debug("sync query task={} ", task);
        return execTask(task);
    }

    public DBPagingResult query(String _sql, int _page_size, int _page_index) throws Exception{
        return query(dbConfig, _sql, _page_size, _page_index);
    }

    /**
     * 每次任务检查是否有过期缓存，有则删除
     * @throws Exception
     */
    private void checkTimeout() throws Exception{
        List<History> list = new LinkedList<>();

        for(History history : historyMap.values())
            if(history.isDirty())
                list.add(history);

        for(History history : list){
            String table_name = history.tableName;
            logger.info("remove dirty cache {}", table_name);
            db3.delTable(table_name);
            historyMap.remove(table_name);
        }
    }

    private DBPagingResult execTask(Task _task) throws Exception{
        String table_name = _task.tableName;
        History history = historyMap.get(table_name);

        int row_count;
        ResultSet rs;
        if(null!=history && !history.isDirty()){
            row_count = history.rowCount;
        }else{
            String conn_key = _task.config.getKey();
            Connection connection = connectionMap.get(conn_key);
            if(null== connection || connection.isClosed()) {
                logger.debug("newConnection {}", _task.config);
                connection = DBHelper.newConnection(_task.config);
                connectionMap.put(conn_key, connection);
            }
            Statement stmt = connection.createStatement();
            logger.debug("query {}",_task.sql);
            rs = stmt.executeQuery(_task.sql);

            String fields = getFieldNames(rs);
            row_count = db3.put(table_name, rs);
            if(null==history){
                history = new History(_task.tableName, fields, row_count);
                historyMap.put(table_name, history);
            }
            history.lastActiveTime = System.currentTimeMillis();
        }

        int page_count= 0;
        if(_task.pageSize >0) {
            page_count = row_count % _task.pageSize;
            if(page_count * _task.pageSize < row_count)
                page_count++;
        }

        String sql = String.format(" select %s from %s ", history.fieldNames, table_name);
        if(_task.pageSize > 0)
            sql += String.format(" limit %d offset %d ", _task.pageSize, _task.pageSize*(_task.pageIndex-1) );
        rs = db3.get(sql);

        return new DBPagingResult(row_count, page_count, _task.pageIndex, rs);
    }

    public static void main(String[] args) throws Exception{
        DBPagingQuery pagingQuery = new DBPagingQuery();

        DBConfig config = new DBConfig(DBDriver.MYSQL);
        config.host = "192.168.4.250";
        config.port = 3306;
        config.name = "etimage";
        config.user = "root";
        config.pass = "root";
        pagingQuery.setup("d:/temp/aa/paging.db3", config);

        IDBPagingCallback callback = new IDBPagingCallback() {
            @Override
            public void onResult(DBPagingResult _result) {
                logger.debug("rows={}, page.size={}, page.index={}", _result.rowsCount, _result.pageCount, _result.pageIndex);
                try {
                    StringBuilder sb = new StringBuilder();
                    ResultSet rs = _result.result;
                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();
                    while(rs.next()) {
                        DB3Cache.row2Line(sb, meta, rs, cols);
                        logger.debug("{}", sb.toString());
                        sb.delete(0, sb.length());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        String sql = "select vehicle_id, gps_time, camera_no, longitude, latitude, media_format, event_code, file_name "
        		+ "from sys_image_2015_04 "
        		+ "where vehicle_id in (9, 10, 11) and gps_time BETWEEN \"2015-04-01 00:00:01\" and \"2015-04-30 12:59:59\"";
        
        DBPagingResult res = pagingQuery.query(sql, 5, 1);
        callback.onResult(res);
        logger.debug("after auery sync");
        
        for(int i=1; i<10; i++)
            pagingQuery.query(sql, 5, i, callback);
        
        logger.debug("after auery async");
        //Thread.sleep(10*1000);
    }

}
