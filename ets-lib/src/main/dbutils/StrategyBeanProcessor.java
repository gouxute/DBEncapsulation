package com.etrans.lib.dbutils;

import org.apache.commons.dbutils.BeanProcessor;

import java.beans.PropertyDescriptor;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * 实现类的get和set方法跟数据库表中有下划线命名字段的映射，进行赋值获取结果
 * 参考自 http://dean-liu.iteye.com/blog/588753
 * @author 刘晓阳 策略模式的BeanProcesso
 * @since 2010-02-05
 */

public class StrategyBeanProcessor extends BeanProcessor {
	
	private Matcher matcher;
	
	public StrategyBeanProcessor(){
		// Ĭ��Matcher
		matcher = new EqualsIgnoreCaseMatcher();
	}
	
	public StrategyBeanProcessor(Matcher matcher){
		this.matcher = matcher;
	}
	
	public Matcher getMatcher() {
		return matcher;
	}

	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	/**
	 * ��дBeanProcessor��ʵ��,ʹ�ò���ģʽ
	 */
	protected int[] mapColumnsToProperties(ResultSetMetaData rsmd,
            PropertyDescriptor[] props) throws SQLException {
		if (matcher == null)
			throw new IllegalStateException("Matcher must be setted!");
		
        int cols = rsmd.getColumnCount();
        int columnToProperty[] = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

        for (int col = 1; col <= cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
              columnName = rsmd.getColumnName(col);
            }
            for (int i = 0; i < props.length; i++) {
                if (matcher.match(columnName, props[i].getName())) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }

        return columnToProperty;
    }
}
