package com.etrans.lib.dbutils;


import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 获取数据库连接，单个数据库连接
 * Created by maotz on 2015/3/17.
 */
public class ConnectionManager {
    private BoneCP connectionPool = null;

    private String dbDriver;
    private String dbUrl;
    private String dbUser;
    private String dbPwd;
    private Integer dbPartitionCount;


    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPwd() {
        return dbPwd;
    }

    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }

    public Integer getDbPartitionCount() {
        return dbPartitionCount;
    }

    public void setDbPartitionCount(Integer dbPartitionCount) {
        this.dbPartitionCount = dbPartitionCount;
    }

    //单例变量
    private static class ConnectionManagerHolder {
        private static ConnectionManager instance = new ConnectionManager();
    }

    //私有化的构造方法，保证外部的类不能通过构造器来实例化。
    private ConnectionManager() {
    }

    //获取单例对象实例
    public static ConnectionManager getInstance() {
        return ConnectionManagerHolder.instance;
    }

    /**
     * 初始化数据库连接池
     */
    public void init() throws ClassNotFoundException, SQLException {
        Class.forName(dbDriver);
        BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPwd);
        config.setMinConnectionsPerPartition(5);
        config.setMaxConnectionsPerPartition(10);
        config.setPartitionCount(dbPartitionCount);
        connectionPool = new BoneCP(config);
    }

    //获取数据库连接
    public synchronized Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }
}