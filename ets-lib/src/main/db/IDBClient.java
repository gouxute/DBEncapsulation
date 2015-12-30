package com.etrans.lib.db;

/**
 * Created by maotz on 2015-03-28.
 *  数据访问客户端，接口
 */
public interface IDBClient {
    /**
     * 设置服务地址
     * @param _host 主机
     * @param _port 端口
     */
    void start(String _host, int _port);
    /**
     * 注册结果监听器
     * @param _listener 监听器
     */
    void registerListener(IDBResultListener _listener);

    /**
     * 发送请求
     * @param _request 请求消息
     */
    void sendRequest(DBRequest _request);
}
