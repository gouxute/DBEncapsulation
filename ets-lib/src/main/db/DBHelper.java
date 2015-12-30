package com.etrans.lib.db;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.etrans.lib.utils.Zip;

import java.sql.*;
import java.util.Properties;

/**
 * Created by maotz on 2015-03-27.
 * Assistant class for access data,
 *  1、加载数据库驱动
 *  2、构建连接字符串
 *  3、新建数据库连接
 */
public final class DBHelper {
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DBHelper.class);

    private static boolean checkReacheable(String _host, int _port){
        return true;
        /*
        boolean reachable = false;
        try{
            InetAddress address = InetAddress.getByName(_host);
            reachable = address.isReachable(5000);
            //logger.info("{} is reachable", _host);
        }catch (UnknownHostException e) {
            logger.error("unknown host {} {}",_host, e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        if(!reachable){
            logger.info("unreachabe to port:{}:{} ", _host, _port);
        }
        return reachable;
        //*/
    }

    /**
     * 新建链接对象
     * @param _config 数据库连接参数
     * @return 链接对象
     * @throws Exception
     */
    public static Connection newConnection(DBConfig _config) throws Exception{
        if(null == _config){
            logger.error("DBHelper newConnection _config is null.");
            return null;
        }

        Connection connection = null;
        DBDriver driver = _config.getDriver();

        if(DBDriver.SQLITE==driver || checkReacheable(_config.host, _config.port)){
            String driver_class = driver.getDriverClass();
            Class.forName(driver_class);
            String url = driver.getConnectionURL(_config.host, _config.port, _config.name);
            logger.info("create db connection {}", url);

            // 2015-04-27,  添加日期格式，by mous
            if(DBDriver.SQLITE == driver){
                Properties properties = new Properties();
                properties.setProperty("date_string_format", "yyyy-MM-dd HH:mm:ss");
                connection = DriverManager.getConnection(url, properties);
            }else {
                connection = DriverManager.getConnection(url, _config.user, _config.pass);
            }
        }
        return connection;
    }

    /**
     * 将数据集，转换为JSON对象
     * @param _rs 结果集
     * @return JSON 对象
     */
    public static JSONArray resultSet2JSON(ResultSet _rs) throws SQLException, JSONException {
        JSONArray json_ary = new JSONArray();

        // 获取列数
        ResultSetMetaData meta_data = _rs.getMetaData();
        int col_count = meta_data.getColumnCount();

        // 遍历ResultSet中的每条数据
        while (_rs.next()) {
            JSONObject json_obj = new JSONObject();

            // 遍历每一列
            for (int i = 1; i <= col_count; i++) {
                String col_name = meta_data.getColumnLabel(i);
                String value = _rs.getString(col_name);

                //< 数据特殊处理
                String data_type = meta_data.getColumnTypeName(i);
                if (data_type.toUpperCase().equals("DATETIME")) {
                    value = value.substring(0, 19);
                }
                //> 数据特殊处理

                json_obj.put(col_name, value);
            }
            json_ary.add(json_obj);
        }
        return json_ary;
    }

    /**
     * JSON序列化辅助函数
     * @param _json_ary JSON对象数组
     * @return 压缩流
     */
    public static String JSONArrayToZip(JSONArray _json_ary){
        String result = null;
        try {
            String json_str = _json_ary.toJSONString();
            byte[] bytes = json_str.getBytes("UTF8");
            bytes = Zip.encode(bytes, 0, bytes.length);
            result = new String(bytes, "UTF8");
        }catch (Exception e){
            logger.error("json to zip error ", e);
        }
        return result;
    }

    /**
     * 压缩的二进制流，变为JSON数组
     * @param _data 二进制楼
     * @return JSON 数组
     */
    public static JSONArray JSONArrayByZip(String _data){
        JSONArray result = null;
        try {
            byte[] bytes = _data.getBytes("UTF8");
            bytes = Zip.decode(bytes, 0, bytes.length);
            result = JSON.parseArray(new String(bytes, "UTF8"));
        }catch (Exception e){
            logger.error("json to zip error ", e);
        }
        return result;
    }
}
