package com.etrans.lib.dbutils;

/**
 * 实现类的get和set方法跟数据库表中有下划线命名字段的映射，进行赋值获取结果
 * 参考自 http://dean-liu.iteye.com/blog/588753
 * @author 刘晓阳
 * @since 2010-02-05
 */
public class HumpMatcher implements Matcher {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yang.commons.dbutils.Matcher#match(java.lang.String,
	 * java.lang.String)
	 */
	public boolean match(String columnName, String propertyName) {
		if (columnName == null)
			return false;

		columnName = columnName.toLowerCase();
		String[] _ary = columnName.split("_");
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < _ary.length; i++) {
			String str = _ary[i];
			if (!"".equals(str) && i > 0) {
				StringBuilder _builder = new StringBuilder();
				str = _builder.append(str.substring(0, 1).toUpperCase()).append(str.substring(1)).toString();
			}
			strBuilder.append(str);
		}
		return strBuilder.toString().equals(propertyName);
	}

}
