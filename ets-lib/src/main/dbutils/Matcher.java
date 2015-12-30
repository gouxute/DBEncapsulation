package com.etrans.lib.dbutils;


/**
 * 实现类的get和set方法跟数据库表中有下划线命名字段的映射，进行赋值获取结果
 * 参考自 http://dean-liu.iteye.com/blog/588753
 * @author 刘晓阳
 * @since 2010-02-05
 */
public interface Matcher {
	
	/**
	 * �Ƿ�ƥ��
	 * @param columnName �ֶ���
	 * @param propertyName ������
	 * @return ƥ����
	 */
	boolean match(String columnName, String propertyName);
}
