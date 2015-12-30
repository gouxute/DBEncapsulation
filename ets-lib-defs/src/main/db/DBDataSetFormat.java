package com.etrans.lib.db;

/**
 * Created by mous on 2015-03-27.
 * 结果集数据格式
 */
public enum DBDataSetFormat {
    /**
     * 无结果
     */
    DATASET_FORMAT_EMPTY(0),
    /**
     * JSON 格式
     */
    DATASET_FORMAT_JSON(1),
    /**
     * 文本格式
     */
    DATASET_FORMAT_TXT(2),
    /**
     * XML格式
     */
    DATASET_FORMAT_XML(3)
    ;

    /**
     * 内部值
     */
    private final int value;

    /**
     * 构造构造函数
     * @param _value 整数值
     */
    DBDataSetFormat(int _value){
        value = _value;
    }

    /**
     * 取内部整数值
     * @return 整数值
     */
    public int getValue(){
        return value;
    }

    @Override
    public String toString(){
        switch (this){
            case DATASET_FORMAT_EMPTY   : return "NULL";
            case DATASET_FORMAT_JSON    : return "JSON";
            case DATASET_FORMAT_TXT     : return "TXT";
            case DATASET_FORMAT_XML     : return "XML";
            default                     : return "NULL";
        }
    }

    /**
     * 按数值取类型
     * @param _value 数值
     * @return 类型
     */
    public static DBDataSetFormat valueOf(int _value){
        switch (_value){
            case 0  : return DATASET_FORMAT_EMPTY;
            case 1  : return DATASET_FORMAT_JSON;
            case 2  : return DATASET_FORMAT_TXT;
            case 3  : return DATASET_FORMAT_XML;
            default : return DATASET_FORMAT_EMPTY;
        }
    }

}
