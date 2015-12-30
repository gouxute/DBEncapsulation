package com.etrans.lib.db;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by mous on 2015-03-31.
 * 请求的附加参数
 */
public class DBReqArg {
    private final JSONObject body = new JSONObject();

    public DBReqArg(){
        setDataFormat(DBDataSetFormat.DATASET_FORMAT_JSON);
        setTimeout(10);
    }

    /**
     * 设置超时时间
     * @param _timeout 超时门限（秒）
     */
    public void setTimeout(int _timeout){
        body.put("timeout", _timeout);
    }

    /**
     * 取超时设置
     * @return 门限，秒数
     */
    public int getTimeout(){
        return body.getIntValue("timeout");
    }

    /**
     * 设置数据库名称
     * @param _db_name 数据库名
     */
    public void setDBName(String _db_name){
        body.put("dbname", _db_name);
    }

    /**
     * 取数据库名称
     * @return 库名称
     */
    public String getDBName(){
        return body.getString("dbname");
    }

    /**
     * 设置结果集类型
     * @param _format 格式
     */
    public void setDataFormat(DBDataSetFormat _format){
        if(null!=_format && DBDataSetFormat.DATASET_FORMAT_JSON != _format)
            body.put("fmt", _format.toString());
    }

    /**
     * 取结果集数据格式
     * @return 数据格式
     */
    public DBDataSetFormat getDataFormat(){
        DBDataSetFormat result = DBDataSetFormat.DATASET_FORMAT_JSON;
        if(body.get("fmt")!=null) {
            int fmt = body.getIntValue("fmt");
            result = DBDataSetFormat.valueOf(fmt);
        }
        return result;
    }

    /**
     * 设置属性
     * @param _key 关键字
     * @param _val 数据值
     */
    public void setAttr(String _key, Object _val){
        body.put(_key, _val);
    }

    /**
     * 取属性值
     * @return 值
     */
    public Object getAttr(String _key){
        return body.get(_key);
    }

    @Override
    public String toString(){
        return body.toJSONString();
    }
}
