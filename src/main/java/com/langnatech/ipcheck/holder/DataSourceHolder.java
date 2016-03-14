package com.langnatech.ipcheck.holder;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

public class DataSourceHolder {
	private static BasicDataSource ds = null;
	private static final Logger logger = Logger.getLogger(DataSourceHolder.class);
	static{
		try {
			initDataSource();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initDataSource() throws ConfigurationException{
		ds = new BasicDataSource();
		Configuration config = new PropertiesConfiguration("ipcheck.properties");
		ds.setDriverClassName(config.getString("jdbc.ipms.driver"));
		ds.setUrl(config.getString("jdbc.ipms.url"));
		ds.setUsername(config.getString("jdbc.ipms.username"));
		ds.setPassword(config.getString("jdbc.ipms.password"));
		ds.setMaxActive(10);
		ds.setMaxIdle(5);
		ds.setTestOnBorrow(true);
		ds.setValidationQuery("select 1 from DUAL");
		ds.setTestWhileIdle(true);
		logger.info("init datasource,  NumActive:" + ds.getMaxActive() + ",NumIdle:" + ds.getMaxIdle());
	}

	public static DataSource getDatasource() throws Exception {
		if (ds == null || ds.isClosed()) {
			initDataSource();
		}
		return ds;
	}

	public static void destory() throws SQLException  {
		if (null != ds && !ds.isClosed()) {
			ds.close();
		}
	}
}
