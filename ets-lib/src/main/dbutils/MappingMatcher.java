package com.etrans.lib.dbutils;
import java.util.HashMap;
import java.util.Map;


/**
 * 实现类的get和set方法跟数据库表中有下划线命名字段的映射，进行赋值获取结果
 * 参考自 http://dean-liu.iteye.com/blog/588753
 * @author 刘晓阳
 * @since 2010-02-05
 */
public class MappingMatcher implements Matcher {
	
	private Map<String, String> map = null;
	
	public MappingMatcher(String [][] mapping){
		if (mapping == null)
			throw new IllegalArgumentException();
		
		map = new HashMap<String, String>();
		for (int i = 0; i < mapping.length; i++){
			String columnName = mapping[i][0];
			if (columnName != null)
				map.put(columnName.toUpperCase(), mapping[i][1]);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.yang.commons.dbutils.Matcher#match(java.lang.String, java.lang.String)
	 */
	public boolean match(String columnName, String propertyName) {
		if (columnName == null)
			return false;
		String pName = map.get(columnName.toUpperCase());
		if (pName == null)
			return false;
		else {
			return pName.equals(propertyName);
		}
	}
}
