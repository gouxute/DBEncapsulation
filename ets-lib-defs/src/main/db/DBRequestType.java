package com.etrans.lib.db;

/**
 * Created by mous on 2015-03-27.
 * 数据库任务类型
 */
public enum DBRequestType {
    /**
     * 未知任务
     */
    DB_REQUEST_UNKNOW(0),
    /**
     * 查询任务
     */
    DB_REQUEST_QUERY(1),
    /**
     * 执行任务
     */
    DB_REQUEST_EXECUTE(2),
    /**
     * 导入任务
     */
    DB_REQUEST_BULK(3),
    /**
     * 取消任务
     */
    DB_REQUEST_CANCEL(4)
    ;

    /**
     * 内部值
     */
    private final int value;

    /**
     * 构造函数
     * @param _value 整数值
     */
    DBRequestType(int _value){
        value = _value;
    }

    /**
     * 取内部整数值
     * @return 整数值
     */
    public int getValue(){
        return value;
    }

    /**
     * 根据整数返回类型
     * @param _value 整数值
     * @return 任务类型
     */
    public static DBRequestType valueOf(int _value){
        switch (_value){
            case 0  : return DB_REQUEST_UNKNOW;
            case 1  : return DB_REQUEST_QUERY;
            case 2  : return DB_REQUEST_EXECUTE;
            case 3  : return DB_REQUEST_BULK;
            case 4  : return DB_REQUEST_CANCEL;
            default : return DB_REQUEST_UNKNOW;
        }
    }

    @Override
    public String toString(){
        switch (this){
            case DB_REQUEST_UNKNOW : return "未知";
            case DB_REQUEST_QUERY  : return "查询";
            case DB_REQUEST_EXECUTE: return "执行";
            case DB_REQUEST_BULK   : return "导入";
            case DB_REQUEST_CANCEL : return "取消";
            default                 : return "未知";
        }
    }
}
