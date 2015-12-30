package com.etrans.lib.db;

import com.etrans.lib.kryo.KryoObj;

/**
 * Created by maotz on 2015-03-27.
 * 数据库操作请求
 */
public final class DBResult extends KryoObj{
    /**
     * 日志记录对象
     */
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DBResult.class);

    /**
     * 请求信息，在初始化时赋值
     */
    private int requestID;

    /**
     * 结果是否执行成功
     */
    boolean success = false;

    /**
     * 结果描述信息
     */
    String message;

    /**
     * 结果的内容
     */
    private DBDataSet data;

    /**
     * 无参构造函数，序列化用
     */
    public DBResult(){
    }

    /**
     * 构造函数
     *
     * @param _request_id 请求
     */
    public DBResult(int _request_id){
        requestID = _request_id;
    }

    public static DBResult makeTimeout(int _request_id){
        DBResult result = new DBResult(_request_id);
        result.setMessage("timeout");
        return result;
    }

    /**
     * 取请求信息
     * @return 请求序号
     */
    public int getRequestID() {
        return requestID;
    }

    /**
     * 是否执行成功
     * @return 成功返回 true，失败返回 false
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * 执行结果描述
     * @return 描述内容
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置结果描述
     * @param _msg 描述信息
     */
    public void setMessage(String _msg){
        message = _msg;
    }

    /**
     * 取结果数据
     * @return 结果
     */
    public DBDataSet getData() {
        return data;
    }

    /**
     * 设置JSON数组
     * @param _data 结果集合
     */
    void setData(DBDataSet _data){
        data = _data;
    }

    @Override
    public String toString(){
        return String.format("RES{req=%s, ok=%s, msg=%s, dat=%s}", requestID, success, message, data );
    }

}
