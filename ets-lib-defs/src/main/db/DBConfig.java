package com.etrans.lib.db;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by mous on 14-7-28.
 *
 *  数据访问配置
 *  仅在服务端使用，不应该开发到客户端
 */
public class DBConfig {
    /**
     * 数据库类型
     */
    private DBDriver driver;
    /**
     * 服务器地址
     */
    public String host;
    /**
     * 数据库端口
     */
    public int port = 0;
    /**
     * 数据库名称
     */
    public String name;
    /**
     * 登录用户
     */
    public String user;
    /**
     * 登录密码
     */
    public String pass = "";
    /**
     * 工作线程数
     */
    public int threads = 1;

    DBConfig(){
    }

    public DBConfig(DBDriver _driver){
        this.driver = _driver;
        this.host = "localhost";
        this.name = "test";
        switch (_driver){
            case MYSQL:
                user = "root";
                port = 3306;
                break;
            case MSSQL:
                user = "sa";
                port = 1433;
                break;
            case ORACLE:
                port = 1521;
                break;
            default:
                break;
        }
    }

    /**
     * 克隆方法，复制一份
     * @return 新的对象
     */
    public DBConfig clone(){
        DBConfig config = new DBConfig(driver);
        config.host = this.host;
        config.port = this.port;
        config.name = this.name;
        config.user = this.user;
        config.pass = this.pass;
        return config;
    }

    public DBDriver getDriver() {
        return driver;
    }

    /**
     * 获取配置关键字
     * @return 关键字
     */
    public String getKey(){
        StringBuilder sb = new StringBuilder();
        sb.append(driver.getName()).append("/").append(host).append(":").append(port);
        sb.append("#").append(name).append("$").append(user).append(":").append(pass);
        return sb.toString();
    }

    @Override
    public String toString(){
        return getKey();
    }

    public static void main(String[] args) throws Exception{
        DBConfig cfg = new DBConfig(DBDriver.MSSQL);
        String jo =  JSONObject.toJSONString(cfg, true);
        DBConfig aa = JSON.parseObject(jo, DBConfig.class);
        System.out.println(jo + aa);
    }

}
