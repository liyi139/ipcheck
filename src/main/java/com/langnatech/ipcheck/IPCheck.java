package com.langnatech.ipcheck;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import com.langnatech.ipcheck.collect.CollectFileToDB;
import com.langnatech.ipcheck.collect.IPCollecter;
import com.langnatech.ipcheck.holder.DataSourceHolder;
import com.langnatech.ipcheck.pool.IpPoolService;

public class IPCheck {
	private static Logger logger = Logger.getLogger(IPCheck.class);

	public static void main(String[] args) {
		String step = args.length > 0 ? args[0] : null;
		try {
			if (step == null || step.equalsIgnoreCase("initIPTemp")) {
				// 根据IP地址池,生成全量IP地址临时表
				logger.info("::::1.BEGIN初始化生成地址池全量IP地址临时表::::");
				IpPoolService ipPoolService = new IpPoolService();
				ipPoolService.initFullAddressTable();
				logger.info("::::1.END::::::::::::::::::::::::::::::::::");
			}
			if (step == null || step.equalsIgnoreCase("collectToFile")) {
				// 通过snmp协议从设备上采集地址,并插入临时表
				logger.info("::::2.BEGIN 设备采集,采集结果保存文件:::::::::");
				IPCollecter.collect();
				logger.info("::::2.END::::::::::::::::::::::::::::::::::");
			}
			if (step == null || step.equalsIgnoreCase("collectToDB")) {
				// 通过snmp协议从设备上采集地址,并插入临时表
				logger.info("::::3.BEGIN 将采集结果文件读取入库,插入临时表::::");
				CollectFileToDB.readFileToDB();
				logger.info("::::3.END::::::::::::::::::::::::::::::::::");
			}
			if (step == null || step.equalsIgnoreCase("check")) {
				// 通过临时表关联,对ip地址进行核查,并讲核查结果插入核查结果表
				logger.info("::::4.BEGIN IP地址核查比对,插入核查结果表::::");
				int warnCount = IPCheck.checkIP();
				logger.info("::::4.END IP地址核查异常地址数量:" + warnCount+"::::");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int checkIP() throws Exception {
		QueryRunner run = new QueryRunner(DataSourceHolder.getDatasource());
		String sql = "TRUNCATE TABLE IP_CHECK_LOG";
		run.update(sql);
		sql = "INSERT INTO IP_CHECK_LOG  SELECT  " + "     IFNULL(T.ADDRESS_IP, T.CHECK_IP) AS IP_ADDRESS, "
				+ "     T.SUBNET_ID, " + "     T.CITY_ID, " + "     T.POOL_ID, "
				+ "     T.ADDRESS_STATUS AS IP_STATUS, "
				+ " 	CASE WHEN (T.ADDRESS_STATUS = 2 AND T.CHECK_IP IS NULL) THEN 2 "
				+ " 		 WHEN (T.CHECK_IP IS NOT NULL AND T.ADDRESS_IP IS NULL) THEN 3 "
				+ " 		 WHEN (T.CITY_ID != T.CHECK_CITY AND T.CITY_ID IS NOT NULL AND T.CHECK_CITY IS NOT NULL) THEN 1 "
				+ " 	ELSE -1 END AS WARN_TYPE, " + "     T.CHECK_CITY, " + "     T.CHECK_DEV, " + "     SYSDATE() "
				+ " FROM " + "     (SELECT * FROM IP_TMP_FULL_ADDRESS T1 "
				+ "     LEFT OUTER JOIN IP_TMP_CHECK T2 ON T1.ADDRESS_IP = T2.CHECK_IP  " + " 	UNION  "
				+ " 	SELECT * FROM ip_tmp_full_address T1 "
				+ "     RIGHT OUTER JOIN IP_TMP_CHECK T2 ON T1.ADDRESS_IP = T2.CHECK_IP) T " + " WHERE "
				+ " (T.ADDRESS_TYPE iS NULL OR T.ADDRESS_TYPE=2) and  " + " ( "
				+ " 		(T.ADDRESS_STATUS = 2 AND T.CHECK_IP IS NULL) "
				+ "         OR (T.CHECK_IP IS NOT NULL AND T.ADDRESS_IP IS NULL) "
				+ "         OR (T.CITY_ID != T.CHECK_CITY AND T.CITY_ID IS NOT NULL AND T.CHECK_CITY IS NOT NULL) "
				+ " 	) ";
		return run.update(sql);
	}
}
