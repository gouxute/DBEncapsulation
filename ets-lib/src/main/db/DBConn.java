package com.etrans.lib.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by maotz on 2015-04-10.
 * Database connection
 */
public class DBConn {
    /**
     * 日志对象
     */
    final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DBConn.class);

    /***
     * 数据库连接对象
     */
    final Connection connection;
    /**
     * 配置信息
     */
    final DBConfig config;
    /**
     * 创建时间
     */
    long createTime;
    /**
     * 活动时间
     */
    long activeTime;

    /**
     * 构造函数
     * @param _connection 链接对象
     * @param _config     配置信息
     */
    public DBConn(Connection _connection, DBConfig _config){
        connection  = _connection;
        config      = _config;
        createTime  = System.currentTimeMillis();
        activeTime  = createTime;
    }

    /**
     * 查询是否关闭
     * @return 关闭状态，true：已关闭，false：未关闭
     */
    public boolean isClosed(){
        boolean is_closed = false;
        try {
            is_closed = connection.isClosed();
        } catch (SQLException e) {
            logger.error("error on check closed", e);
        }
        return is_closed;
    }

    /**
     * 关闭链接
     */
    public void close(){
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error("error on close connection", e);
        }
    }

    /**
     * 执行请求，返回结果
     * @param _request 请求
     * @return  _执行结果
     */
    public DBResult execute(DBRequest _request) throws Exception{
        DBResult result = new DBResult(_request.getRequestID());
        try {
            String sql = _request.getSql();
            logger.info("SQL={{}}", sql);
            Statement statement = connection.createStatement();
            switch (_request.getRequestType()) {
                case DB_REQUEST_QUERY:
                    ResultSet rs = statement.executeQuery(sql);
                    //logger.debug("获取结果，进行转换");
                    DBDataSet dataSet = new DBDataSet(_request.getDataFormat(), rs);
                    rs.close();
                    result.setData(dataSet);
                    result.message = "查询成功";
                    result.success = true;
                    break;
                case DB_REQUEST_EXECUTE:
                    statement.execute(sql);
                    result.message = "执行成功";
                    result.success = true;
                    break;
                default:
                    logger.info("unknown request {}", _request);
            }
            statement.close();
        } catch (SQLException e) {
            result.message = e.getMessage();
            logger.error("execute failed {} {}",_request,  e);
        }
        return result;
    }

    @Override
    public String toString(){
        return String.format("%s|%s", config.toString(), connection);
    }
}
