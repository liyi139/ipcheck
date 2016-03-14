package com.langnatech.ipcheck.collect;

import org.apache.commons.dbutils.QueryRunner;

import com.langnatech.ipcheck.holder.DataSourceHolder;

public class IPCheck {
	public void check() throws Exception{
		QueryRunner run = new QueryRunner(DataSourceHolder.getDatasource());
		String sql="";
	}
}
