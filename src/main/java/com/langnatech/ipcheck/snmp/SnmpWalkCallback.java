package com.langnatech.ipcheck.snmp;

public interface SnmpWalkCallback {
	public void handleIP(String oid, String variable) throws Exception;
}
