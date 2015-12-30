package com.etrans.lib.db;

import com.etrans.lib.kryo.KryoObj;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by maotz on 2015-03-27.
 * 数据库操作结果
 */
public class DBRequest extends KryoObj{
    /**
     * 请求产生时间
     */
    private final long genTime = System.currentTimeMillis();
    /**
     * 请求序号
     */
    private int requestID;
    /**
     * 任务类型
     */
    private DBRequestType requestType;
    /**
     * 语句
     */
    private String sqlStmt;
    /**
     * 附加参数
     */
    private final DBReqArg extArg = new DBReqArg();

    /**
     * 无参构造函数，序列化用
     */
    public DBRequest(){
    }

    /**
     * 构造函数
     * @param _request_id 请求序号
     * @param _request_type 任务类型
     * @param _sql_stmt 语句
     */
    private DBRequest(int _request_id, DBRequestType _request_type, String _sql_stmt){
        requestID = _request_id;
        requestType = _request_type;
        sqlStmt = _sql_stmt;
    }

    /**
     * 请求序号，全局变量
     */
    private static AtomicInteger requestSeq = new AtomicInteger(0);

    /**
     * 构造一个查询请求
     * @param _sql 查询语句
     * @return 请求对象
     */
    public static DBRequest makeQueryRequest(String _sql){
        requestSeq.compareAndSet(Integer.MAX_VALUE - 1, 0);
        return new DBRequest(requestSeq.getAndIncrement(), DBRequestType.DB_REQUEST_QUERY, _sql);
    }

    /**
     * 构造一个执行请求
     * @param _sql 查询语句
     * @return 请求对象
     */
    public static DBRequest makeExecuteRequest(String _sql){
        requestSeq.compareAndSet(Integer.MAX_VALUE - 1, 0);
        return new DBRequest(requestSeq.getAndIncrement(), DBRequestType.DB_REQUEST_EXECUTE, _sql);
    }

    /**
     * 构造一个取消请求
     * @param _request 被原理的请求
     * @return 取消请求
     */
    public static DBRequest makeCancelRequest(DBRequest _request){
        return new DBRequest(_request.requestID, DBRequestType.DB_REQUEST_CANCEL, "");
    }

    /**
     * 取请求序号
     * @return 序号
     */
    public int getRequestID() {
        return requestID;
    }

    /**
     * 取生请求年龄
     * @return 请求语句产生多少毫秒
     */
    public long getAge() {
        return System.currentTimeMillis() - genTime;
    }

    /**
     * 判断是否过期
     * @return true：过期， false：未过期
     */
    public boolean hasExpired(){
        int timeout = extArg.getTimeout() * 1000;
        return (timeout > 0) && (getAge() > timeout);
    }

    /**
     * 取任务类型
     * @return 任务类型
     */
    public DBRequestType getRequestType() {
        return requestType;
    }

    /**
     * 取结果集类型
     * @return 数据格式
     */
    public DBDataSetFormat getDataFormat(){
        return extArg.getDataFormat();
    }

    /**
     * 取操作语句
     * @return SQL语句
     */
    public String getSql() {
        return sqlStmt;
    }

    /**
     * 取附加参数
     * @return 附加参数
     */
    public DBReqArg getArg(){
        return extArg;
    }


    @Override
    public String toString(){
        return String.format("REQ(type=%s, seq=%d, sql={%s}, arg=%s)", requestType, requestID, sqlStmt, extArg);
    }
}
