package com.etrans.lib.db;

import java.sql.ResultSet;

/**
 * Created by maotz on 2015-03-27.
 * Database dataset
 */
public final class DBDataSet {
    /**
     * 数据集类型
     */
    private DBDataSetFormat dataFormat = DBDataSetFormat.DATASET_FORMAT_EMPTY;

    /**
     * 数据
     */
    private String dataBody;

    /**
     * 无参构造函数，序列化用
     */
    public DBDataSet(){
    }

    /**
     * 传入数据集作为结果
     * @param _format 数据集类型
     * @param _result 数据集
     */
    public DBDataSet(DBDataSetFormat _format, ResultSet _result) throws Exception{
        dataFormat = _format;
        switch (dataFormat) {
            case DATASET_FORMAT_JSON:
                dataBody = DBHelper.resultSet2JSON(_result).toJSONString();
                break;
            default:
        }
    }

    /**
     * 返回数据集类型
     * @return 数据类型
     */
    public DBDataSetFormat getDataFormat(){
        return dataFormat;
    }

    /**
     * 取数据对象
     * @return 数据内容
     */
    public String getDataBody() {
        return dataBody;
    }

    @Override
    public String toString(){
        return String.format("%s:[%s]",  dataFormat, null!=dataBody?dataBody.length():0);
    }
}
