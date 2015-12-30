package com.etrans.lib.dbutils;

/**
 * 实现类的get和set方法跟数据库表中有下划线命名字段的映射，进行赋值获取结果
 * 参考自 http://dean-liu.iteye.com/blog/588753
 * @author 刘晓阳
 * @since 2010-02-05
 */
public class EqualsIgnoreCaseMatcher implements Matcher {

	/*
	 * (non-Javadoc)
	 * @see com.yang.commons.dbutils.Matcher#match(java.lang.String, java.lang.String)
	 */
	public boolean match(String columnName, String propertyName) {
		if (columnName == null)
			return false;
		else {
			return columnName.equals(propertyName);
		}
	}

}
