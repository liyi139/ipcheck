package com.test.snmp;

import org.junit.Test;

import com.langnatech.ipcheck.pool.IpPoolService;

public class TestDS {
	@Test
	public void testDatasource() throws Exception{
		IpPoolService ipPoolService=new IpPoolService();
		ipPoolService.initFullAddressTable();
	}
}
